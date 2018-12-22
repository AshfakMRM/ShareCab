package com.example.ashfak.sharecab;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private TextView userRegistration;
    private EditText email, password;
    private Button btnLog,btnLog2;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private String emailC, passwordC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        userRegistration = (TextView)findViewById(R.id.tvRegister);
        email = (EditText)findViewById(R.id.etEmail);
        password = (EditText)findViewById(R.id.etPassword);
        btnLog = (Button)findViewById(R.id.btnLogin);
        btnLog2 = (Button)findViewById(R.id.btnLogin2);

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        /*if(user != null){
            finish();
            startActivity(new Intent(LoginActivity.this,HomeActivity.class));
        }*/

        startService(new Intent(LoginActivity.this, onAppKilled.class));

        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check()){
                    validate(email.getText().toString(), password.getText().toString());
                }

            }
        });

        btnLog2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check()){
                    validate2(email.getText().toString(), password.getText().toString());
                }

            }
        });



        userRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });

    }

    private void validate(String email, String password){
        progressDialog.setMessage("For more details about ShareCab, Visit our website");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this,"Login Successful as a Driver",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this,HomeMapsActivity.class));
                    //String userId = firebaseAuth.getCurrentUser().getUid();
                    //DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("current_users").child("drivers").child(userId);
                    //currentUserDb.setValue(true);

                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this,"Login Failed",Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    private void validate2(String email, String password){
        progressDialog.setMessage("For more details about ShareCab, Visit our website");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this,"Login Successful as a Rider",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this,HomeMapsActivity2.class));
                    //String userId = firebaseAuth.getCurrentUser().getUid();
                    //DatabaseReference currentUserDb2 = FirebaseDatabase.getInstance().getReference().child("current_users").child("riders").child(userId);
                    //currentUserDb2.setValue(true);

                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this,"Login Failed",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private boolean check(){
        boolean result = false;
        emailC = email.getText().toString();
        passwordC = password.getText().toString();

        if(emailC.isEmpty() || passwordC.isEmpty()){
            Toast.makeText(this,"Please enter all the details", Toast.LENGTH_SHORT).show();
        }
        else {
            result = true;
        }
        return result;
    }

    @Override
    protected void onStop() {
        super.onStop();
        String userId = firebaseAuth.getCurrentUser().getUid();
        DatabaseReference currentUserDb = FirebaseDatabase.getInstance().getReference().child("current_users").child("drivers").child(userId);
        currentUserDb.removeValue();
        DatabaseReference currentUserDb2 = FirebaseDatabase.getInstance().getReference().child("current_users").child("riders").child(userId);
        currentUserDb2.removeValue();
    }
}
