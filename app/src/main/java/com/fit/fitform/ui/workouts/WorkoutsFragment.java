package com.fit.fitform.ui.workouts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.fit.fitform.R;
import com.fit.fitform.data.database.DatabaseInitializer;
import com.fit.fitform.data.database.FitFormDatabase;
import com.fit.fitform.data.entity.Exercise;
import com.fit.fitform.data.entity.Workout;
import com.fit.fitform.data.repository.WorkoutRepository;
import com.fit.fitform.databinding.FragmentWorkoutsBinding;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Workouts Fragment for displaying and managing user workouts.
 * Shows available workouts, allows creation of custom workouts, and provides workout selection.
 * Includes tabs for different workout categories and workout management features.
 */
public class WorkoutsFragment extends Fragment {
    
    private FragmentWorkoutsBinding binding;
    private WorkoutRepository workoutRepository;
    private WorkoutAdapter workoutAdapter;
    private List<Workout> currentWorkouts = new ArrayList<>();
    private String currentCategory = "All";
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize repositories
        FitFormDatabase database = FitFormDatabase.getDatabase(requireContext());
        workoutRepository = new WorkoutRepository(database.workoutDao(), database.exerciseDao());
        
        setupRecyclerView();
        setupTabs();
        setupClickListeners();
        
        // Initialize database with sample data if needed
        initializeDatabaseIfNeeded();
        
        loadWorkouts();
    }
    
    /**
     * Sets up the RecyclerView for displaying workouts
     */
    private void setupRecyclerView() {
        workoutAdapter = new WorkoutAdapter(currentWorkouts, new WorkoutAdapter.OnWorkoutClickListener() {
            @Override
            public void onWorkoutClick(Workout workout) {
                // Start camera exercise for the selected workout
                Intent intent = new Intent(requireContext(), com.fit.fitform.ui.camera.ExerciseCameraActivity.class);
                intent.putExtra("exercise_type", workout.getName());
                intent.putExtra("workout_id", workout.getId());
                startActivity(intent);
            }
            
            @Override
            public void onWorkoutEdit(Workout workout) {
                // TODO: Navigate to workout editor
                Toast.makeText(requireContext(), "Edit workout: " + workout.getName(), Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onWorkoutDelete(Workout workout) {
                // TODO: Show confirmation dialog and delete workout
                Toast.makeText(requireContext(), "Delete workout: " + workout.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        
        binding.workoutsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.workoutsRecyclerView.setAdapter(workoutAdapter);
    }
    
    /**
     * Sets up the tab layout for workout categories
     */
    private void setupTabs() {
        binding.categoryTabs.addTab(binding.categoryTabs.newTab().setText("All"));
        binding.categoryTabs.addTab(binding.categoryTabs.newTab().setText("Strength"));
        binding.categoryTabs.addTab(binding.categoryTabs.newTab().setText("Cardio"));
        binding.categoryTabs.addTab(binding.categoryTabs.newTab().setText("HIIT"));
        binding.categoryTabs.addTab(binding.categoryTabs.newTab().setText("Flexibility"));
        
        binding.categoryTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String category = tab.getText().toString();
                if (category.equals("All")) {
                    currentCategory = "All";
                } else {
                    currentCategory = category;
                }
                loadWorkouts();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    /**
     * Sets up click listeners for UI elements
     */
    private void setupClickListeners() {
        binding.createWorkoutButton.setOnClickListener(v -> {
            // TODO: Navigate to workout creation screen
            Toast.makeText(requireContext(), "Create new workout", Toast.LENGTH_SHORT).show();
        });
        
        binding.refreshButton.setOnClickListener(v -> loadWorkouts());
    }
    
    /**
     * Initializes database with sample data if needed
     */
    private void initializeDatabaseIfNeeded() {
        new Thread(() -> {
            try {
                // Check if we have any pre-built workouts
                List<Workout> existingWorkouts = workoutRepository.getPreBuiltWorkouts().getValue();
                if (existingWorkouts == null || existingWorkouts.isEmpty()) {
                    DatabaseInitializer.initializeDatabase(requireContext());
                }
            } catch (Exception e) {
                // Handle error silently
            }
        }).start();
    }
    
    /**
     * Loads workouts based on current category
     */
    private void loadWorkouts() {
        if (currentCategory.equals("All")) {
            loadAllWorkouts();
        } else {
            loadWorkoutsByCategory(currentCategory);
        }
    }
    
    /**
     * Loads all workouts for the current user
     */
    private void loadAllWorkouts() {
        long userId = getUserId();
        if (userId != -1) {
            workoutRepository.getWorkoutsByUser(userId).observe((LifecycleOwner) requireContext(), new Observer<List<Workout>>() {
                @Override
                public void onChanged(List<Workout> workouts) {
                    if (workouts != null) {
                        currentWorkouts.clear();
                        currentWorkouts.addAll(workouts);
                        workoutAdapter.notifyDataSetChanged();
                        updateEmptyState();
                    }
                }
            });
        } else {
            // Load pre-built workouts for non-logged in users
            workoutRepository.getPreBuiltWorkouts().observe((LifecycleOwner) requireContext(), new Observer<List<Workout>>() {
                @Override
                public void onChanged(List<Workout> workouts) {
                    if (workouts != null) {
                        currentWorkouts.clear();
                        currentWorkouts.addAll(workouts);
                        workoutAdapter.notifyDataSetChanged();
                        updateEmptyState();
                    }
                }
            });
        }
    }
    
    /**
     * Loads workouts by specific category
     * @param category Workout category to filter by
     */
    private void loadWorkoutsByCategory(String category) {
        workoutRepository.getWorkoutsByCategory(category).observe((LifecycleOwner) requireContext(), new Observer<List<Workout>>() {
            @Override
            public void onChanged(List<Workout> workouts) {
                if (workouts != null) {
                    currentWorkouts.clear();
                    currentWorkouts.addAll(workouts);
                    workoutAdapter.notifyDataSetChanged();
                    updateEmptyState();
                }
            }
        });
    }
    
    /**
     * Updates the empty state visibility
     */
    private void updateEmptyState() {
        if (currentWorkouts.isEmpty()) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.workoutsRecyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.workoutsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Gets the current user ID from SharedPreferences
     * @return User ID or -1 if not found
     */
    private long getUserId() {
        return requireContext().getSharedPreferences("user_session", 0)
            .getLong("user_id", -1);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
