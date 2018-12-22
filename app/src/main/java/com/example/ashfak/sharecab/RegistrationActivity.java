package com.example.ashfak.sharecab;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegistrationActivity extends AppCompatActivity {

    private EditText userName, userEmail, userMobileNo, userPassword;
    private Button btnReg;
    private TextView userLogin;
    private FirebaseAuth firebaseAuth;
    String name,email,password,mobileno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setupUIViews();
        firebaseAuth = FirebaseAuth.getInstance();

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    //upload to database
                    String user_email = userEmail.getText().toString().trim();
                    String user_password = userPassword.getText().toString().trim();

                    firebaseAuth.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendUserData();
                                Toast.makeText(RegistrationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));

                            }
                            else{
                                Toast.makeText(RegistrationActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        userLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegistrationActivity.this, LoginActivity.class));
            }
        });
    }

    private void sendUserData(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        String userId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference databaseReference = firebaseDatabase.getReference().child("users").child(userId);
        UserProfile userProfile = new UserProfile(name,email,mobileno);
        databaseReference.setValue(userProfile);



    }

    private void setupUIViews(){
        userName = (EditText)findViewById(R.id.etUsername);
        userEmail = (EditText)findViewById(R.id.etUserEmail);
        userMobileNo = (EditText)findViewById(R.id.etUserMobileNo);
        userPassword = (EditText)findViewById(R.id.etUserPassword);
        btnReg = (Button)findViewById(R.id.btnRegister);
        userLogin = (TextView)findViewById(R.id.tvLogin);

    }

    private boolean validate(){
        boolean result = false;
        name = userName.getText().toString();
        email = userEmail.getText().toString();
        mobileno = userMobileNo.getText().toString();
        password = userPassword.getText().toString();

        if(name.isEmpty() || email.isEmpty() || mobileno.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Please enter all the details", Toast.LENGTH_SHORT).show();
        }
        else {
            result = true;
        }
        return result;
    }
}
