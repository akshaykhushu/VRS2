package com.bazr2.aksha.newb;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.apache.commons.lang3.ObjectUtils;
import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    Button signInButton;
    EditText email;
    EditText password;
    TextView register;
    TextView forgotPassword;
    private FirebaseAuth firebaseAuth;
    String userId;
    ImageButton googleSignIn;
    private final static int RC_SIGN_IN = 1234;
    GoogleSignInOptions gso;
    GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signInButton = findViewById(R.id.buttonSignIn);
        email = findViewById(R.id.editTextEmail);
        password = findViewById(R.id.editTextPassword);
        register = findViewById(R.id.textViewRegister);
        googleSignIn = findViewById(R.id.imageButtonGoogleSignIn);
        firebaseAuth = FirebaseAuth.getInstance();
        forgotPassword = findViewById(R.id.textViewForgotPassword);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                Toast.makeText(getApplicationContext(), "Something Went Wrong", Toast.LENGTH_SHORT);
            }
        }).addApi(Auth.GOOGLE_SIGN_IN_API, gso).build();

        if (firebaseAuth.getCurrentUser() != null){
            if (firebaseAuth.getCurrentUser().isEmailVerified()){
                finish();
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                userId =  firebaseAuth.getCurrentUser().getUid();
                intent.putExtra("UserId", userId);
                startActivity(intent);
            }
            else {
                Toast.makeText(getApplicationContext(), "Pls Verify your Email", Toast.LENGTH_SHORT).show();
            }
        }

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
                startActivity(intent);
            }
        });


    }

    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            else{
                Toast.makeText(getApplicationContext(), "Something Went Wrong", Toast.LENGTH_SHORT);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    try{
                        if (firebaseAuth.getCurrentUser().isEmailVerified())
                            Toast.makeText(getApplicationContext(), "Welcome " + firebaseAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();
                        else{
                            Toast.makeText(getApplicationContext(), "Pls verify your email", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                    catch(NullPointerException e){
                        Toast.makeText(getApplicationContext(), "Welcome " + firebaseAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                    }
                    finish();
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    userId =  firebaseAuth.getCurrentUser().getUid();
                    intent.putExtra("UserId", userId);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Could Not Sign In. Try Again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void signIn(){
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

        firebaseAuth.signInWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                        try{
                            Toast.makeText(getApplicationContext(), "Welcome " + firebaseAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                        }
                        catch(NullPointerException e){
                            Toast.makeText(getApplicationContext(), "Welcome " + firebaseAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                        }
                        finish();
                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        userId =  firebaseAuth.getCurrentUser().getUid();
                        intent.putExtra("UserId", userId);
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Please verify your email", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Incorrect UserName or Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
