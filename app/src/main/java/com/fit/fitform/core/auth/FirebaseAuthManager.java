package com.fit.fitform.core.auth;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.fit.fitform.data.model.UserProfile;

/**
 * Firebase Authentication Manager for handling user authentication and profile management.
 * Provides methods for sign up, sign in, sign out, and saving user profile data to Firestore.
 */
public class FirebaseAuthManager {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;

    public FirebaseAuthManager() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public Task<AuthResult> signIn(@NonNull String email, @NonNull String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> signUp(@NonNull String email, @NonNull String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    /**
     * Signs in with Google using an ID token
     * @param idToken The Google ID token
     * @return Task with AuthResult
     */
    public Task<AuthResult> signInWithGoogle(@NonNull String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        return firebaseAuth.signInWithCredential(credential);
    }

    public void signOut() {
        firebaseAuth.signOut();
    }

    /**
     * Updates the Firebase user's display name
     * @param displayName The display name to set
     * @param listener Success listener
     * @param failureListener Failure listener
     */
    public void updateUserDisplayName(@NonNull String displayName,
                                     OnSuccessListener<Void> listener,
                                     OnFailureListener failureListener) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnSuccessListener(listener)
                    .addOnFailureListener(failureListener);
        } else {
            if (failureListener != null) {
                failureListener.onFailure(new Exception("No user is currently signed in"));
            }
        }
    }

    /**
     * Saves user profile data to Firestore
     * @param userProfile The user profile to save
     * @param listener Success listener
     * @param failureListener Failure listener
     */
    public void saveUserProfile(@NonNull UserProfile userProfile,
                               OnSuccessListener<Void> listener,
                               OnFailureListener failureListener) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // Use the Firebase Auth UID as the document ID
            firestore.collection("users")
                    .document(user.getUid())
                    .set(userProfile.toMap())
                    .addOnSuccessListener(listener)
                    .addOnFailureListener(failureListener);
        } else {
            if (failureListener != null) {
                failureListener.onFailure(new Exception("No user is currently signed in"));
            }
        }
    }

    /**
     * Gets user profile from Firestore
     * @param userId The user ID
     * @return Task with DocumentSnapshot containing user profile data
     */
    public com.google.android.gms.tasks.Task<com.google.firebase.firestore.DocumentSnapshot> getUserProfile(@NonNull String userId) {
        return firestore.collection("users")
                .document(userId)
                .get();
    }
}


