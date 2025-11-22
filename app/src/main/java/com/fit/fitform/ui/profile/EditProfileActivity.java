package com.fit.fitform.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.fit.fitform.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;

public class EditProfileActivity extends AppCompatActivity {
    
    private ActivityEditProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage storage;
    private Uri selectedImageUri;
    
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null && data.getData() != null) {
                        selectedImageUri = data.getData();
                        Glide.with(this)
                                .load(selectedImageUri)
                                .circleCrop()
                                .into(binding.profileImageView);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }

        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        
        loadUserProfile();
        setupClickListeners();
    }

    private void loadUserProfile() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            binding.usernameEditText.setText(user.getDisplayName());
            binding.emailText.setText(user.getEmail());
            
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .into(binding.profileImageView);
            }
        }
    }

    private void setupClickListeners() {
        binding.changePhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImage.launch(intent);
        });

        binding.saveButton.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        String newUsername = binding.usernameEditText.getText().toString().trim();
        
        if (newUsername.isEmpty()) {
            binding.usernameLayout.setError("Username cannot be empty");
            return;
        }

        // Show progress
        binding.saveButton.setEnabled(false);
        binding.saveButton.setText("Saving...");

        if (selectedImageUri != null) {
            // Upload image first
            StorageReference profileRef = storage.getReference()
                    .child("profile_images")
                    .child(user.getUid() + ".jpg");

            profileRef.putFile(selectedImageUri)
                    .continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return profileRef.getDownloadUrl();
                    })
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            updateProfile(newUsername, downloadUri);
                        } else {
                            updateProfile(newUsername, null);
                        }
                    });
        } else {
            updateProfile(newUsername, null);
        }
    }

    private void updateProfile(String newUsername, Uri newPhotoUri) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) return;

        UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername);
        
        if (newPhotoUri != null) {
            profileUpdates.setPhotoUri(newPhotoUri);
        }

        user.updateProfile(profileUpdates.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        binding.saveButton.setEnabled(true);
                        binding.saveButton.setText("Save Changes");
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}