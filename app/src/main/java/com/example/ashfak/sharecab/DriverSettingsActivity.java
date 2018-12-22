package com.example.ashfak.sharecab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DriverSettingsActivity extends AppCompatActivity {
    private EditText nameD, mobileD, carD;
    private Button confirm, back;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private String userID;
    private String name, mobile, profileUrl, car, drivingMode;
    private ImageView profilePic;
    private Uri resultUri;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_settings);

        nameD = (EditText)findViewById(R.id.etNameD);
        mobileD = (EditText)findViewById(R.id.etMobileD);
        carD = (EditText)findViewById(R.id.etCar);

        confirm = (Button) findViewById(R.id.btnConfirmD);
        back = (Button) findViewById(R.id.btnBackD);

        profilePic = (ImageView)findViewById(R.id.profilePicRider);

        radioGroup = (RadioGroup)findViewById(R.id.rgDrivingMode);

        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        getUserInfo();

        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/❛");
                startActivityForResult(intent, 1);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
                Toast.makeText(DriverSettingsActivity.this,"Updating Your Profile...",Toast.LENGTH_LONG).show();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });



    }

    private void getUserInfo(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                Map<String,Object> map = (Map<String,Object>)dataSnapshot.getValue();
                //if(map.get("userName")!=null){
                name = map.get("userName").toString();
                nameD.setText(name);
                //}
                // if(map.get("userMobileNo")!=null){
                mobile = map.get("userMobileNo").toString();
                mobileD.setText(mobile);
                //}
                if(map.get("car")!=null){
                car = map.get("car").toString();
                carD.setText(car);
                }
                if(map.get("drivingMode")!=null){
                    drivingMode = map.get("drivingMode").toString();
                    switch (drivingMode){
                        case "Private":
                            radioGroup.check(R.id.rbPrivate);
                            break;

                        case "Shared":
                            radioGroup.check(R.id.rbShared);
                            break;

                    }
                }
                if(map.get("profileImageUrl")!=null){
                    profileUrl = map.get("profileImageUrl").toString();
                    Glide.with(getApplication()).load(profileUrl).into(profilePic);
                }

                //}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInformation(){
        name = nameD.getText().toString();
        mobile = mobileD.getText().toString();
        car = carD.getText().toString();

        int selectId = radioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton = (RadioButton)findViewById(selectId);

        if (radioButton.getText() == null){
            return;
        }

        drivingMode = radioButton.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("userName",name);
        userInfo.put("userMobileNo",mobile);
        userInfo.put("car",car);
        userInfo.put("drivingMode",drivingMode);
        databaseReference.updateChildren(userInfo);

        if (resultUri!=null){
            final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("profile images").child(userID);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(),resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20,baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map newImage = new HashMap();
                            newImage.put("profileImageUrl", uri.toString());
                            databaseReference.updateChildren(newImage);

                            finish();
                            return;
                        }
                    });

                }
            });

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });
        }
        else {
            finish();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            final Uri imageUri = data.getData();
            resultUri = imageUri;
            profilePic.setImageURI(resultUri);
        }
    }
}


