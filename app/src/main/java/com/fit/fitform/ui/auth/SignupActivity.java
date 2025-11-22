package com.fit.fitform.ui.auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fit.fitform.R;
import com.fit.fitform.core.analytics.AnalyticsManager;
import com.fit.fitform.ui.main.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {
    TextInputEditText edtEmail;
    TextInputEditText edtPassword;
    TextInputEditText edtUsername;
    Button signupBtn;
    String username, email, password;
    FirebaseAuth firebaseAuth;
    private AnalyticsManager analyticsManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        analyticsManager = AnalyticsManager.getInstance(this);
        
        edtEmail = findViewById(R.id.emailEditText);
        edtPassword = findViewById(R.id.passwordEditText);
        edtUsername = findViewById(R.id.usernameEditText);
        signupBtn = findViewById(R.id.signupButton);

        // Login link click handler
        TextView loginLink = findViewById(R.id.loginLink);
        if (loginLink != null) {
            loginLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
                }
            });
        }

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = edtUsername.getText().toString().trim();
                email = edtEmail.getText().toString().trim();
                password = edtPassword.getText().toString().trim();

                if(TextUtils.isEmpty(username)){
                    edtUsername.setError("Please enter your username");
                    edtUsername.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(email)){
                    edtEmail.setError("Please enter your email");
                    edtEmail.requestFocus();
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    edtPassword.setError("Please enter your password");
                    edtPassword.requestFocus();
                    return;
                }
                if(password.length() < 6){
                    edtPassword.setError("Password must be at least 6 characters");
                    edtPassword.requestFocus();
                    return;
                }
                signUp();
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
        }
    }

    private void signUp(){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Update user profile with username
                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username).build();
                        
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if(firebaseUser != null){
                            // Track signup event
                            analyticsManager.logSignup("email");
                            analyticsManager.setUserId(firebaseUser.getUid());
                            
                            firebaseUser.updateProfile(userProfileChangeRequest)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                            // Navigate to MainActivity directly after successful profile update
                                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(SignupActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = "Signup failed: " + e.getMessage();
                        if(e.getMessage() != null && e.getMessage().contains("email address is already in use")){
                            errorMessage = "Email is already registered. Please login instead.";
                        }
                        Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
