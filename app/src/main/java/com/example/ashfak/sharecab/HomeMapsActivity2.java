package com.example.ashfak.sharecab;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeMapsActivity2 extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    private Button logout, request, settings, history;
    private LatLng pickupLocation, destinationLatlng;
    private String destination, drivingMode2;
    private SupportMapFragment mapFragment;
    private LinearLayout driverInfo;
    private ImageView driverImage;
    private TextView driverName, driverMobile, driverCar;
    private RadioGroup radioGroup;
    private Boolean requestBol = false;
    private Marker pickupMarker;
    private RatingBar ratingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(HomeMapsActivity2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        else {
            mapFragment.getMapAsync(this);
        }

        destinationLatlng = new LatLng(0.0,0.0);

        driverInfo = (LinearLayout)findViewById(R.id.driverInfo);
        driverImage = (ImageView) findViewById(R.id.driverImage);
        driverName = (TextView) findViewById(R.id.driverName);
        driverMobile = (TextView) findViewById(R.id.driverMobileNo);
        driverCar = (TextView)findViewById(R.id.driverCar);
        radioGroup = (RadioGroup)findViewById(R.id.rgDrivingMode2);
        radioGroup.check(R.id.rbShared2);

        logout = (Button)findViewById(R.id.btnLogout);
        request = (Button)findViewById(R.id.btnRequest);
        settings = (Button)findViewById(R.id.btnSettings);
        history = (Button)findViewById(R.id.btnHistory);

        ratingBar = (RatingBar)findViewById(R.id.ratingBar2);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(HomeMapsActivity2.this,LoginActivity.class));
                finish();
                return;
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestBol){
                    endRide();
                }

                else {
                    requestBol = true;
                    int selectId = radioGroup.getCheckedRadioButtonId();
                    final RadioButton radioButton = (RadioButton)findViewById(selectId);

                    if (radioButton.getText() == null){
                        return;
                    }

                    drivingMode2 = radioButton.getText().toString();

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("requests");
                    GeoFire geoFire = new GeoFire(databaseReference);
                    geoFire.setLocation(userId,new GeoLocation(lastLocation.getLatitude(),lastLocation.getLongitude()));

                    pickupLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.place)));
                    request.setText("Getting Your Driver...");
                    getClosestDriver();
                }


            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeMapsActivity2.this, RiderSettingsActivity.class);
                startActivity(intent);
                return;

            }
        });

        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeMapsActivity2.this, HistoryActivity.class);
                intent.putExtra("riderOrDriver" , "rider");
                startActivity(intent);
                return;
            }
        });


        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName().toString();
                destinationLatlng = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
            }
        });
    }
    private int radius = 1;
    private boolean driverFound = false;
    private String driverFoundId;
    GeoQuery geoQuery;
    private void getClosestDriver(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("drivers available");
        GeoFire geoFire = new GeoFire(driverLocation);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude,pickupLocation.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound && requestBol){
                    DatabaseReference driverDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(key);
                    driverDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String,Object> driverMap = (Map<String,Object>)dataSnapshot.getValue();
                                if (driverFound){
                                    return;
                                }

                                if(driverMap.get("drivingMode").equals(drivingMode2)){
                                    driverFound = true;
                                    driverFoundId = dataSnapshot.getKey();

                                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("users").child(driverFoundId).child("customer request");
                                    String riderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                    HashMap map = new HashMap();
                                    map.put("customer ride id", riderId);
                                    map.put("destination", destination);
                                    map.put("destinationLat", destinationLatlng.latitude);
                                    map.put("destinationLng", destinationLatlng.longitude);
                                    driverRef.updateChildren(map);
                                    getDriverLocation();
                                    getDriverInfo();
                                    getHasRideEnded();
                                    request.setText("Looking for Driver Location...");
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }
    private Marker driverMarker;
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation(){
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("drivers working").child(driverFoundId).child("l");
        driverLocationRefListener =  driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && requestBol){
                    List<Object> map = (List<Object>)dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;

                    if (map.get(0)!=null){
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1)!=null){
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatlng = new LatLng(locationLat,locationLng);
                    if (driverMarker!=null){
                        driverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatlng.latitude);
                    loc2.setLongitude(driverLatlng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    if(distance<100){
                        request.setText("Driver is Here");
                    }

                    else {
                        request.setText("Driver Found : " +String.valueOf(distance)+" meters");
                    }



                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatlng).title("Your Driver").icon(BitmapDescriptorFactory.fromResource(R.mipmap.car)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getDriverInfo(){
        driverInfo.setVisibility(View.VISIBLE);
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("users").child(driverFoundId);
        driverRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                Map<String,Object> map = (Map<String,Object>)dataSnapshot.getValue();
                //if(map.get("userName")!=null){
                driverName.setText(map.get("userName").toString() );
                //}
                // if(map.get("userMobileNo")!=null){
                driverMobile.setText(map.get("userMobileNo").toString());
                //}
                driverCar.setText(map.get("car").toString());
                if(map.get("profileImageUrl")!=null){
                    Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(driverImage);
                }
                int ratingSum = 0;
                float ratingTotal = 0;
                float ratingAvg = 0;
                for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                    ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                    ratingTotal++;
                }
                if (ratingTotal!=0){
                    ratingAvg = ratingSum/ratingTotal;
                    ratingBar.setRating(ratingAvg);
                }

                //}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driverHasEndedRefListener;

    private void getHasRideEnded(){
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("users").child(driverFoundId).child("customer request").child("customer ride id");
        driverHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }
                else {
                    endRide();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void endRide(){
        requestBol = false;
        geoQuery.removeAllListeners();
        driverLocationRef.removeEventListener(driverLocationRefListener);
        driveHasEndedRef.removeEventListener(driverHasEndedRefListener);

        if (driverFoundId != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("users").child(driverFoundId).child("customer request");
            driverRef.removeValue();
            driverFoundId = null;
        }
        driverFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("requests");
        GeoFire geoFire = new GeoFire(databaseReference);
        geoFire.removeLocation(userId);

        if (pickupMarker != null){
            pickupMarker.remove();
        }
        if (driverMarker != null){
            driverMarker.remove();
        }
        request.setText("Request Ride");
        driverInfo.setVisibility(View.GONE);
        driverName.setText("");
        driverMobile.setText("");
        driverCar.setText("");
        driverImage.setImageResource(R.drawable.addprofile);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(HomeMapsActivity2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);

        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

    }

    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null) {

            lastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(HomeMapsActivity2.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    final int LOCATION_REQUEST_CODE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                }else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission" , Toast.LENGTH_LONG);
                }
                break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
