package com.bazr2.aksha.newb;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    Button signUp;
    FirebaseAuth firebaseAuth;
    public static String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.editTextRegisterEmail);
        password = findViewById(R.id.editTextRegisterPassword);
        signUp = findViewById(R.id.buttonSignUp);
        firebaseAuth = FirebaseAuth.getInstance();
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }


    public void registerUser(){
        String emailStr = email.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();

        if(TextUtils.isEmpty(emailStr)){
            Toast toast = Toast.makeText(this, "Please Enter Email ID", Toast.LENGTH_SHORT);
            toast.show();
            return;

        }

        if(TextUtils.isEmpty(passwordStr)){
            Toast toast = Toast.makeText(this, "Please Enter Password", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (passwordStr.length() < 8) {
            Toast.makeText(this, "Password Length must be greater than 8 letters", Toast.LENGTH_SHORT).show();
            return;
        }



        firebaseAuth.createUserWithEmailAndPassword(emailStr,passwordStr).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "An Email has been sent to your email id. Pls verify your email.", Toast.LENGTH_SHORT).show();
                    finish();
//                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
//                    userId =  firebaseAuth.getCurrentUser().getUid();
//                    intent.putExtra("UserId", userId);
//                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Could Not Register. Try Again", Toast.LENGTH_SHORT).show();
                }
            }
        });

        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                finish();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                userId =  firebaseUser.getUid();
                                intent.putExtra("UserId", userId);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        });

    }
}
