package com.fit.fitform.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fit.fitform.R;
import com.fit.fitform.core.analytics.AnalyticsManager;
import com.fit.fitform.ui.main.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText edtEmail, edtPassword;
    private Button loginBtn;
    private SignInButton googleSignInButton;
    private TextView signupLink;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private AnalyticsManager analyticsManager;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Google Sign-In result received. Result code: " + result.getResultCode());
                
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Log.d(TAG, "Processing Google Sign-In result...");
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    
                    // Use addOnCompleteListener to handle the task asynchronously
                    task.addOnCompleteListener(this, completedTask -> {
                        try {
                            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
                            if (account != null) {
                                Log.d(TAG, "Google Sign-In successful! Account: " + account.getEmail());
                                Log.d(TAG, "Starting Firebase authentication...");
                                firebaseAuthWithGoogle(account);
                            } else {
                                Log.e(TAG, "GoogleSignInAccount is null");
                                Toast.makeText(LoginActivity.this, "Google Sign-In failed: No account received", Toast.LENGTH_SHORT).show();
                            }
                        } catch (ApiException e) {
                            int statusCode = e.getStatusCode();
                            Log.e(TAG, "Google sign in failed with status code: " + statusCode, e);
                            
                            String errorMessage = "Google sign in failed.";
                            switch (statusCode) {
                                case 10: // DEVELOPER_ERROR
                                    errorMessage = "Configuration error. Check Web Client ID and SHA-1 fingerprint.";
                                    break;
                                case 12501: // SIGN_IN_CANCELLED
                                    Log.d(TAG, "User cancelled Google Sign-In");
                                    return; // Don't show error for cancellation
                                case 7: // NETWORK_ERROR
                                    errorMessage = "Network error. Check your connection.";
                                    break;
                                default:
                                    errorMessage = "Sign in failed. Please try again.";
                                    break;
                            }
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Log.d(TAG, "Google Sign-In cancelled by user.");
                } else {
                    Log.w(TAG, "Google Sign-In failed with result code: " + result.getResultCode());
                    Toast.makeText(LoginActivity.this, "Google Sign-In failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        analyticsManager = AnalyticsManager.getInstance(this);
        
        edtEmail = findViewById(R.id.emailEditText);
        edtPassword = findViewById(R.id.passwordEditText);
        loginBtn = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        signupLink = findViewById(R.id.signupLink);

        configureGoogleSignIn();

        loginBtn.setOnClickListener(v -> loginUser());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        signupLink.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    private void configureGoogleSignIn() {
        try {
            String webClientId = getString(R.string.default_web_client_id);
            if (webClientId.contains("YOUR_WEB_CLIENT_ID_HERE") || webClientId.isEmpty()) {
                Log.e(TAG, "Web Client ID not set in strings.xml. Google Sign-In will fail.");
                mGoogleSignInClient = null;
                return;
            }
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            Log.d(TAG, "Google Sign-In configured successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error configuring Google Sign-In. Is R.string.default_web_client_id set in your strings.xml?", e);
            mGoogleSignInClient = null;
        }
    }

    private void signInWithGoogle() {
        if (mGoogleSignInClient == null) {
            Toast.makeText(this, "Google Sign-In is not configured. Please check your project setup.", Toast.LENGTH_LONG).show();
            return;
        }
        // Ensure previous Google session is cleared so user can choose a different account
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        if (acct == null) {
            Log.e(TAG, "Cannot authenticate with Firebase: Google account is null.");
            Toast.makeText(this, "Authentication failed: Invalid account data.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get ID token - should be available after successful Google Sign-In
        String idToken = acct.getIdToken();
        
        if (idToken == null || idToken.isEmpty()) {
            Log.e(TAG, "ID token is null or empty. This usually indicates a configuration issue.");
            Log.e(TAG, "Please verify:");
            Log.e(TAG, "1. Web Client ID is correctly set in strings.xml");
            Log.e(TAG, "2. Google Sign-In is enabled in Firebase Console");
            Log.e(TAG, "3. SHA-1 fingerprint is added to Firebase Console");
            Toast.makeText(this, "Authentication failed: ID token not available. Check configuration.", Toast.LENGTH_LONG).show();
            return;
        }
        
        Log.d(TAG, "ID token received, length: " + idToken.length());
        authenticateWithFirebase(idToken);
    }

    private void authenticateWithFirebase(String idToken) {
        Log.d(TAG, "Creating Firebase credential with ID token...");
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Firebase authentication successful. User: " + user.getEmail() + ", UID: " + user.getUid());
                            // Track Google Sign-In login event
                            analyticsManager.logLogin("google");
                            analyticsManager.setUserId(user.getUid());
                        }
                        Log.d(TAG, "Navigating to MainActivity...");
                        navigateToMainActivity();
                    } else {
                        Exception exception = task.getException();
                        Log.e(TAG, "Firebase authentication failed.", exception);
                        String errorMsg = "Firebase Authentication Failed.";
                        if (exception != null && exception.getMessage() != null) {
                            errorMsg = exception.getMessage();
                            Log.e(TAG, "Error details: " + errorMsg);
                        }
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email and password cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email login successful.");
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Track login event
                            analyticsManager.logLogin("email");
                            analyticsManager.setUserId(user.getUid());
                        }
                        navigateToMainActivity();
                    } else {
                        Log.e(TAG, "Email login failed", task.getException());
                        Toast.makeText(LoginActivity.this, "Email login failed. Please check credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMainActivity() {
        // Verify user is authenticated before navigating
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Cannot navigate: User is not authenticated");
            Toast.makeText(this, "Authentication error. Please try again.", Toast.LENGTH_LONG).show();
            return;
        }
        
        Log.d(TAG, "═══════════════════════════════════════════════════════════");
        Log.d(TAG, "✓ LOGIN SUCCESSFUL - Navigating to MainActivity");
        Log.d(TAG, "  User: " + currentUser.getEmail());
        Log.d(TAG, "  UID: " + currentUser.getUid());
        Log.d(TAG, "  Destination: com.example.fitform.ui.main.MainActivity");
        Log.d(TAG, "═══════════════════════════════════════════════════════════");
        
//        Toast.makeText(this, "Login Successful! Going to MainActivity...", Toast.LENGTH_SHORT).show();
        
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
