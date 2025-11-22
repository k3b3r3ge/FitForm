package com.fit.fitform.ui.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.fit.fitform.data.entity.Exercise;
import com.fit.fitform.databinding.FragmentWorkoutsBinding;
import java.util.ArrayList;
import java.util.List;

public class WorkoutsFragment extends Fragment implements ExerciseAdapter.OnExerciseClickListener {
    
    private FragmentWorkoutsBinding binding;
    private ExerciseAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkoutsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadExercises();
    }

    private void setupRecyclerView() {
        adapter = new ExerciseAdapter(requireContext(), new ArrayList<>(), this);
        binding.exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.exercisesRecyclerView.setAdapter(adapter);
    }

    private void loadExercises() {
        List<Exercise> exercises = new ArrayList<>();
        
        // Add Push-up exercise
        exercises.add(new Exercise(
            1, // workoutId
            "Push-ups",
            "Classic upper body exercise targeting chest, shoulders, and triceps",
            "1. Start in plank position with hands shoulder-width apart\n" +
            "2. Lower your body until chest nearly touches the ground\n" +
            "3. Push back up to starting position\n" +
            "4. Keep your core tight and body straight throughout",
            "Chest, Shoulders, Triceps",
            "None",
            3, // sets
            10, // reps
            60, // rest time in seconds
            1 // order
        ));

        // Add Squat exercise
        exercises.add(new Exercise(
            1, // workoutId
            "Squats",
            "Fundamental lower body exercise targeting quads, hamstrings, and glutes",
            "1. Stand with feet shoulder-width apart\n" +
            "2. Lower your body by bending knees and hips\n" +
            "3. Keep chest up and back straight\n" +
            "4. Return to standing position",
            "Quads, Hamstrings, Glutes",
            "None",
            3,
            12,
            60,
            2
        ));

        // Add Plank exercise
        exercises.add(new Exercise(
            1, // workoutId
            "Plank",
            "Core strengthening exercise that improves stability",
            "1. Start in forearm plank position\n" +
            "2. Keep body in straight line from head to heels\n" +
            "3. Engage core and maintain position\n" +
            "4. Hold for specified duration",
            "Core, Shoulders",
            "None",
            3,
            30, // seconds per hold
            45,
            3
        ));

        // Set form analysis and GIF URLs
        for (Exercise exercise : exercises) {
            exercise.setHasFormAnalysis(true);
            exercise.setGifUrl("raw/" + exercise.getName().toLowerCase().replace(" ", "_") + ".gif");
        }

        adapter.updateExercises(exercises);
    }

    @Override
    public void onExerciseClick(Exercise exercise) {
        Intent intent = new Intent(requireContext(), ExerciseDetailActivity.class);
        intent.putExtra("exercise", exercise);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}