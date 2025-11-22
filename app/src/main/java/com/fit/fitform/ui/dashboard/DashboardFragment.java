package com.fit.fitform.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import com.fit.fitform.R;
import com.fit.fitform.data.database.FitFormDatabase;
import com.fit.fitform.data.entity.User;
import com.fit.fitform.data.entity.Workout;
import com.fit.fitform.data.repository.UserRepository;
import com.fit.fitform.data.repository.WorkoutRepository;
import com.fit.fitform.databinding.FragmentDashboardBinding;
import android.content.Intent;
import android.widget.Toast;
import android.widget.TextView;
import com.fit.fitform.ui.camera.ExerciseCameraActivity;
import com.google.firebase.auth.FirebaseAuth;
import androidx.lifecycle.Observer;
import java.util.List;
import com.fit.fitform.data.entity.WorkoutSession;
import com.fit.fitform.data.database.FitFormDatabase;

/**
 * Dashboard Fragment displaying user's workout overview and quick stats.
 * Shows welcome message, today's workout, and recent activity summary.
 * Provides navigation to start workouts and view progress.
 */
public class DashboardFragment extends Fragment {
    
    private FragmentDashboardBinding binding;
    private UserRepository userRepository;
    private WorkoutRepository workoutRepository;
    private FitFormDatabase database;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize repositories
        database = FitFormDatabase.getDatabase(requireContext());
        userRepository = new UserRepository(database.userDao());
        workoutRepository = new WorkoutRepository(database.workoutDao(), database.exerciseDao());
        
        setupClickListeners();
        loadUserData();
        loadDashboardData();
    }
    
    /**
     * Sets up click listeners for UI elements
     */
    private void setupClickListeners() {
        binding.startWorkoutButton.setOnClickListener(v -> {
            // Show a simple chooser with the three exercises
            final String[] exercises = new String[] {"Push-up", "Squat", "Plank"};
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
            builder.setTitle("Select exercise")
                .setItems(exercises, (dialog, which) -> {
                    String selected = exercises[which];
                    // Start full-screen camera activity and pass the selected exercise
                    Intent intent = new Intent(requireContext(), ExerciseCameraActivity.class);
                    intent.putExtra("exercise_type", selected);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (d, which) -> d.dismiss())
                .show();
        });
    }
    
    /**
     * Loads and displays user profile data
     */
    private void loadUserData() {
        // Get current Firebase user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String firebaseUid = auth.getCurrentUser().getUid();
            
            new Thread(() -> {
                try {
                    User user = userRepository.getUserByFirebaseUid(firebaseUid);
                    if (user != null && user.getUsername() != null) {
                        requireActivity().runOnUiThread(() -> 
                            binding.userNameText.setText(user.getUsername())
                        );
                    } else {
                        // User not found in Room Database - use Firebase display name as fallback
                        String displayName = auth.getCurrentUser().getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            requireActivity().runOnUiThread(() -> 
                                binding.userNameText.setText(displayName)
                        );
                        }
                    }
                } catch (Exception e) {
                    // Handle error silently - use Firebase display name as fallback
                    String displayName = auth.getCurrentUser().getDisplayName();
                    if (displayName != null && !displayName.isEmpty()) {
                        requireActivity().runOnUiThread(() -> 
                            binding.userNameText.setText(displayName)
                        );
                    }
                }
            }).start();
        }
    }
    
    /**
     * Loads and displays dashboard data including workouts
     */
    private void loadDashboardData() {
        // Get current Firebase user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String firebaseUid = auth.getCurrentUser().getUid();
            
            new Thread(() -> {
                try {
                    User user = userRepository.getUserByFirebaseUid(firebaseUid);
                    if (user != null) {
                        long userId = user.getId();
                        requireActivity().runOnUiThread(() -> {
                            workoutRepository.getWorkoutsByUser(userId).observe(getViewLifecycleOwner(), new Observer<List<Workout>>() {
                                @Override
                                public void onChanged(List<Workout> workouts) {
                                    if (workouts != null && !workouts.isEmpty()) {
                                        Workout todayWorkout = workouts.get(0);
                                        binding.todayWorkoutName.setText(todayWorkout.getName());
                                        binding.todayWorkoutDuration.setText(todayWorkout.getEstimatedDuration() + " minutes");
                                    }
                                }
                            });
                        });
                    }
                } catch (Exception e) {
                    // Handle error silently for now
                }
            }).start();
        }
        
        // Initialize Recent Activity as empty
        setupRecentActivity();
    }
    
    /**
     * Sets up the Recent Activity section to show empty state
     */
    private void setupRecentActivity() {
        long userId = requireContext().getSharedPreferences("user_session", 0).getLong("user_id", -1);
        if (userId <= 0) {
            binding.recentActivityEmptyState.setVisibility(View.VISIBLE);
            binding.recentActivityList.setVisibility(View.GONE);
            return;
        }
        database.workoutSessionDao().getRecentCompletedSessions(userId, 5)
            .observe(getViewLifecycleOwner(), new Observer<List<WorkoutSession>>() {
                @Override
                public void onChanged(List<WorkoutSession> sessions) {
                    binding.recentActivityList.removeAllViews();
                    if (sessions == null || sessions.isEmpty()) {
                        binding.recentActivityEmptyState.setVisibility(View.VISIBLE);
                        binding.recentActivityList.setVisibility(View.GONE);
                    } else {
                        binding.recentActivityEmptyState.setVisibility(View.GONE);
                        binding.recentActivityList.setVisibility(View.VISIBLE);
                        for (WorkoutSession s : sessions) {
                            TextView tv = new TextView(requireContext());
                            tv.setText("Workout #" + s.getWorkoutId() + " - " +
                                (s.getTotalDuration() != null ? s.getTotalDuration() + " min" : "Completed"));
                            tv.setTextColor(getResources().getColor(R.color.text_secondary));
                            tv.setTextSize(14f);
                            int pad = (int) (8 * getResources().getDisplayMetrics().density);
                            tv.setPadding(pad, pad, pad, pad);
                            binding.recentActivityList.addView(tv);
                        }
                    }
                }
            });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
