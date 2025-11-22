package com.fit.fitform.ui.workout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.fit.fitform.data.entity.Exercise;
import com.fit.fitform.databinding.ActivityExerciseDetailBinding;
import com.fit.fitform.ui.camera.ExerciseCameraActivity;

public class ExerciseDetailActivity extends AppCompatActivity {
    
    private ActivityExerciseDetailBinding binding;
    private Exercise exercise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExerciseDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get exercise data from intent
        exercise = getIntent().getParcelableExtra("exercise");
        if (exercise == null) {
            finish();
            return;
        }

        setupViews();
        setupClickListeners();
    }

    private void setupViews() {
        // Set exercise details
        binding.exerciseName.setText(exercise.getName());
        binding.exerciseInstructions.setText(exercise.getInstructions());

        // Load exercise GIF
        Glide.with(this)
            .asGif()
            .load(exercise.getGifUrl())
            .into(binding.exerciseGif);

        // Show camera button only if exercise requires camera
        binding.startExerciseButton.setText(exercise.isRequiresCamera() ? 
            "Start with Camera" : "Start Exercise");
    }

    private void setupClickListeners() {
        binding.startExerciseButton.setOnClickListener(v -> {
            if (exercise.isRequiresCamera()) {
                // Start camera activity for form checking
                Intent intent = new Intent(this, ExerciseCameraActivity.class);
                intent.putExtra("exercise", exercise);
                startActivity(intent);
            } else {
                // Start regular exercise tracking
                startExerciseTracking();
            }
        });
    }

    private void startExerciseTracking() {
        // TODO: Implement exercise tracking without camera
    }
}