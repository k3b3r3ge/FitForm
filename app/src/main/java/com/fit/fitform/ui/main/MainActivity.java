package com.fit.fitform.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.fit.fitform.R;
import com.fit.fitform.databinding.ActivityMainBinding;
import com.fit.fitform.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.fit.fitform.ui.dashboard.DashboardFragment;
import com.fit.fitform.ui.workouts.WorkoutsFragment;
import com.fit.fitform.ui.progress.ProgressFragment;
import com.fit.fitform.ui.profile.ProfileFragment;
import com.fit.fitform.util.WorkoutReminderHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main Activity for the FitForm application.
 * Handles bottom navigation between different app sections.
 * Manages fragment switching and user session validation.
 */
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    
    private ActivityMainBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Check if user is logged in via Firebase
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        setupBottomNavigation();
        
        // Schedule daily workout reminder if not already scheduled
        // Wrapped in try-catch to prevent crashes on Android 13+ permission issues
        try {
            if (!WorkoutReminderHelper.isReminderEnabled(this)) {
                WorkoutReminderHelper.scheduleDailyReminder(this, 8, 0); // 8:00 AM
            }
        } catch (Exception e) {
            // Log but don't crash if alarm scheduling fails
            android.util.Log.e("MainActivity", "Failed to schedule workout reminder", e);
        }
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // On app close/destroy, sign out so next launch returns to Login
        FirebaseAuth.getInstance().signOut();
    }
    
    /**
     * Sets up bottom navigation with click listener
     */
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener(this);
    }
    
    /**
     * Checks if user is currently logged in
     * @return true if user is logged in, false otherwise
     */
    // Session managed by FirebaseAuth.getInstance().getCurrentUser()
    
    /**
     * Handles bottom navigation item selection
     * @param item Selected navigation item
     * @return true if item selection was handled
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment = null;
        
        if (item.getItemId() == R.id.nav_dashboard) {
            fragment = new DashboardFragment();
        } else if (item.getItemId() == R.id.nav_progress) {
            fragment = new ProgressFragment();
        } else if (item.getItemId() == R.id.nav_profile) {
            fragment = new ProfileFragment();
        }
        
        if (fragment != null) {
            loadFragment(fragment);
            return true;
        }
        
        return false;
    }
    
    /**
     * Loads the specified fragment into the main content container
     * @param fragment Fragment to load
     */
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.mainContentContainer, fragment)
            .commit();
    }
}
