package com.example.ashfak.sharecab;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.ashfak.sharecab.rvHistory.HistoryAdapter;
import com.example.ashfak.sharecab.rvHistory.HistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

//import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;


public class HistoryActivity extends AppCompatActivity {

    private String userId, riderOrDriver;
    private RecyclerView rvHistory;
    private RecyclerView.Adapter rvHistoryAdapter;
    private RecyclerView.LayoutManager rvHistoryLayoutManager;
    private ArrayList resultsHistory = new ArrayList<HistoryObject>();
    private TextView balance;
    private Double balanceAmount = 0.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        balance = (TextView)findViewById(R.id.tvBalance);
        rvHistory = (RecyclerView)findViewById(R.id.rvHistory);
        rvHistory.setNestedScrollingEnabled(false);
        rvHistory.setHasFixedSize(true);
        rvHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        rvHistory.setLayoutManager(rvHistoryLayoutManager);
        rvHistoryAdapter = new HistoryAdapter(getDataHistory(), HistoryActivity.this);
        rvHistory.setAdapter(rvHistoryAdapter);

        riderOrDriver = getIntent().getExtras().getString("riderOrDriver");

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();

        if (riderOrDriver.equals("driver")){
            balance.setVisibility(View.VISIBLE);
        }

    }

    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    for (DataSnapshot history : dataSnapshot.getChildren() ){
                        fetchRideInformation(history.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void fetchRideInformation(String rideKey) {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String rideId = dataSnapshot.getKey();
                    Long timestamp = 0L;
                    String distance = "";
                    Double ridePrice = 0.0;

                    for (DataSnapshot child : dataSnapshot.getChildren()){
                        if (child.getKey().equals("timestamp")){
                            timestamp = Long.valueOf(child.getValue().toString());
                        }
                    }

                    if (dataSnapshot.child("customerPaid").getValue() != null && dataSnapshot.child("driverPaidOut").getValue() == null){
                        if (dataSnapshot.child("distance").getValue() != null){
                            distance = dataSnapshot.child("distance").getValue().toString();
                            ridePrice = (Double.valueOf(distance)*0.25);
                            balanceAmount += ridePrice;
                            balance.setText("Balance : " + String.valueOf(balanceAmount) + " $");
                        }

                    }

                    HistoryObject obj = new HistoryObject(rideId, getDate(timestamp));
                    resultsHistory.add(obj);
                    rvHistoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getDate(Long timestamp) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(timestamp*1000);
        String date = DateFormat.format("dd-MM-yyyy hh:mm" , calendar).toString();
        return date;
    }


    private ArrayList<HistoryObject> getDataHistory() {
        return resultsHistory;
    }
}
