package com.fit.fitform.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.fit.fitform.R;
import com.fit.fitform.databinding.FragmentProfileBinding;
import com.fit.fitform.ui.auth.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Profile Fragment for displaying and managing user profile information.
 * Shows user details, provides profile editing options, and handles user logout.
 * Includes settings access and account management features.
 */
public class ProfileFragment extends Fragment {
    
    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private static final int EDIT_PROFILE_REQUEST = 100;
    
    // ActivityResultLauncher for image picker (implicit intent)
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    // Update the profile image view with the selected image
                    Glide.with(this)
                            .load(uri)
                            .circleCrop()
                            .into(binding.profileImageView);
                    
                    // TODO: Optionally upload to Firebase Storage and update user profile
                }
            });
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        firebaseAuth = FirebaseAuth.getInstance();
        setupClickListeners();
        loadUserProfile();
    }
    
    /**
     * Sets up click listeners for UI elements
     */
    private void setupClickListeners() {
        binding.editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });
        
        // Click listener for profile image - launches implicit intent to pick image
        binding.profileImageView.setOnClickListener(v -> pickProfileImage());
        
        binding.logoutButton.setOnClickListener(v -> logout());
    }
    
    /**
     * Launches an implicit intent to pick an image from Photos or other image picker apps
     */
    private void pickProfileImage() {
        // Launch implicit intent with ACTION_GET_CONTENT to open Photos app
        imagePickerLauncher.launch("image/*");
    }
    
    
    /**
     * Loads and displays user profile information
     */
    private void loadUserProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Set the username from Firebase user profile
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                binding.userNameText.setText(displayName);
            }
            
            // Email display removed from UI; keep auth info but do not set a removed view

            // Load profile image if available
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .circleCrop()
                        .into(binding.profileImageView);
            }
        }
    }
    
    /**
     * Handles user logout process
     */
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        
        // Also sign out from Google to avoid auto-selecting the previous account on next sign-in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient googleClient = GoogleSignIn.getClient(requireContext(), gso);
        googleClient.signOut().addOnCompleteListener(task -> {
            // Optionally revoke to clear granted consent so chooser always appears
            googleClient.revokeAccess().addOnCompleteListener(revokeTask -> {
                // Navigate to login
                startActivity(new Intent(requireContext(), LoginActivity.class));
                requireActivity().finish();
            });
        });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == Activity.RESULT_OK) {
            // Reload profile data after successful edit
            loadUserProfile();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
