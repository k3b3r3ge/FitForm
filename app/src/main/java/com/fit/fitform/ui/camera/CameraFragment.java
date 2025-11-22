package com.fit.fitform.ui.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.fit.fitform.R;
import com.fit.fitform.databinding.FragmentCameraBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Camera Fragment for real-time form analysis during workouts.
 * Integrates with ML Kit for pose detection and provides visual feedback on exercise form.
 * Shows red lines for incorrect form and green lines for correct form.
 * Supports multiple exercises with specific form analysis algorithms.
 */
public class CameraFragment extends Fragment {
    
    private static final String TAG = "CameraFragment";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    
    private FragmentCameraBinding binding;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ImageAnalysis imageAnalysis;
    private PoseDetector poseDetector;
    private boolean isAnalysisActive = false;
    private String selectedExercise = "Push-ups";
    
    // Available exercises for form analysis
    private final List<String> availableExercises = Arrays.asList(
        "Push-ups", "Squats", "Plank", "Lunges", "Burpees", "Mountain Climbers"
    );
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupExerciseSelection();
        setupClickListeners();
        setupPoseDetector();
        
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }
    
    /**
     * Sets up the exercise selection dropdown
     */
    private void setupExerciseSelection() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_dropdown_item_1line, availableExercises);
        binding.exerciseSelectionAutoComplete.setAdapter(adapter);
        binding.exerciseSelectionAutoComplete.setText(selectedExercise, false);
    }
    
    /**
     * Sets up click listeners for UI elements
     */
    private void setupClickListeners() {
        binding.toggleAnalysisButton.setOnClickListener(v -> toggleAnalysis());
        binding.switchCameraButton.setOnClickListener(v -> switchCamera());
        binding.captureButton.setOnClickListener(v -> captureImage());
        binding.flashButton.setOnClickListener(v -> toggleFlash());
        
        binding.exerciseSelectionAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            selectedExercise = availableExercises.get(position);
            Log.d(TAG, "Selected exercise: " + selectedExercise);
        });
    }
    
    /**
     * Sets up ML Kit pose detector
     */
    private void setupPoseDetector() {
        AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .build();
        
        poseDetector = PoseDetection.getClient(options);
    }
    
    /**
     * Checks if camera permission is granted
     * @return true if permission is granted, false otherwise
     */
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Requests camera permission from user
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
            new String[]{Manifest.permission.CAMERA},
            CAMERA_PERMISSION_REQUEST_CODE);
    }
    
    /**
     * Starts the camera preview and analysis
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(requireContext());
        
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                Toast.makeText(requireContext(), "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }
    
    /**
     * Binds camera use cases (preview and analysis)
     */
    private void bindCameraUseCases() {
        if (cameraProvider == null) return;
        
        // Preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(binding.cameraPreview.getSurfaceProvider());
        
        // Image analysis use case
        imageAnalysis = new ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build();
        
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), this::analyzeImage);
        
        // Select camera (front or back)
        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        
        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera use cases", e);
        }
    }
    
    /**
     * Analyzes camera image for pose detection
     * @param imageProxy Camera image to analyze
     */
    private void analyzeImage(ImageProxy imageProxy) {
        if (!isAnalysisActive) {
            imageProxy.close();
            return;
        }
        
        InputImage image = InputImage.fromMediaImage(
            imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
        
        poseDetector.process(image)
            .addOnSuccessListener(pose -> {
                processPoseResults(pose);
                imageProxy.close();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Pose detection failed", e);
                imageProxy.close();
            });
    }
    
    /**
     * Processes pose detection results and updates form analysis
     * @param pose Detected pose landmarks
     */
    private void processPoseResults(Pose pose) {
        // Exercise-specific form analysis
        final List<FormAnalysisOverlay.FormFeedback> feedbackList;
        final float overallScore;
        
        switch (selectedExercise) {
            case "Push-ups":
                feedbackList = analyzePushUpForm(pose);
                overallScore = calculatePushUpScore(pose);
                break;
            case "Squats":
                feedbackList = analyzeSquatForm(pose);
                overallScore = calculateSquatScore(pose);
                break;
            case "Plank":
                feedbackList = analyzePlankForm(pose);
                overallScore = calculatePlankScore(pose);
                break;
            default:
                feedbackList = new ArrayList<>();
                overallScore = 0.0f;
                break;
        }
        
        // Update UI on main thread
        requireActivity().runOnUiThread(() -> {
            binding.formAnalysisOverlay.updateFeedback(feedbackList);
            updateFormScore(overallScore);
        });
    }
    
    /**
     * Analyzes push-up form and returns feedback
     * @param pose Detected pose
     * @return List of form feedback items
     */
    private List<FormAnalysisOverlay.FormFeedback> analyzePushUpForm(Pose pose) {
        List<FormAnalysisOverlay.FormFeedback> feedback = new ArrayList<>();
        
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        
        if (leftWrist != null && rightWrist != null && leftShoulder != null && rightShoulder != null) {
            // Check if shoulders are aligned with wrists
            float shoulderWristAlignment = Math.abs(
                (leftShoulder.getPosition().y + rightShoulder.getPosition().y) / 2 -
                (leftWrist.getPosition().y + rightWrist.getPosition().y) / 2
            );
            
            if (shoulderWristAlignment < 50) {
                // Good alignment - green line
                feedback.add(new FormAnalysisOverlay.FormFeedback(
                    leftShoulder.getPosition(), leftWrist.getPosition(),
                    FormAnalysisOverlay.FormFeedbackType.CORRECT));
                feedback.add(new FormAnalysisOverlay.FormFeedback(
                    rightShoulder.getPosition(), rightWrist.getPosition(),
                    FormAnalysisOverlay.FormFeedbackType.CORRECT));
            } else {
                // Poor alignment - red line
                feedback.add(new FormAnalysisOverlay.FormFeedback(
                    leftShoulder.getPosition(), leftWrist.getPosition(),
                    FormAnalysisOverlay.FormFeedbackType.INCORRECT));
                feedback.add(new FormAnalysisOverlay.FormFeedback(
                    rightShoulder.getPosition(), rightWrist.getPosition(),
                    FormAnalysisOverlay.FormFeedbackType.INCORRECT));
            }
        }
        
        return feedback;
    }
    
    /**
     * Analyzes squat form and returns feedback
     * @param pose Detected pose
     * @return List of form feedback items
     */
    private List<FormAnalysisOverlay.FormFeedback> analyzeSquatForm(Pose pose) {
        List<FormAnalysisOverlay.FormFeedback> feedback = new ArrayList<>();
        
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);
        
        if (leftKnee != null && rightKnee != null && leftAnkle != null && rightAnkle != null) {
            // Check knee alignment over ankles
            float leftKneeAnkleAlignment = Math.abs(leftKnee.getPosition().x - leftAnkle.getPosition().x);
            float rightKneeAnkleAlignment = Math.abs(rightKnee.getPosition().x - rightAnkle.getPosition().x);
            
            if (leftKneeAnkleAlignment < 30) {
                feedback.add(new FormAnalysisOverlay.FormFeedback(
                    leftKnee.getPosition(), leftAnkle.getPosition(),
                    FormAnalysisOverlay.FormFeedbackType.CORRECT));
            } else {
                feedback.add(new FormAnalysisOverlay.FormFeedback(
                    leftKnee.getPosition(), leftAnkle.getPosition(),
                    FormAnalysisOverlay.FormFeedbackType.INCORRECT));
            }
            
            if (rightKneeAnkleAlignment < 30) {
                feedback.add(new FormAnalysisOverlay.FormFeedback(
                    rightKnee.getPosition(), rightAnkle.getPosition(),
                    FormAnalysisOverlay.FormFeedbackType.CORRECT));
            } else {
                feedback.add(new FormAnalysisOverlay.FormFeedback(
                    rightKnee.getPosition(), rightAnkle.getPosition(),
                    FormAnalysisOverlay.FormFeedbackType.INCORRECT));
            }
        }
        
        return feedback;
    }
    
    /**
     * Analyzes plank form and returns feedback
     * @param pose Detected pose
     * @return List of form feedback items
     */
    private List<FormAnalysisOverlay.FormFeedback> analyzePlankForm(Pose pose) {
        List<FormAnalysisOverlay.FormFeedback> feedback = new ArrayList<>();
        
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        
        if (leftShoulder != null && rightShoulder != null && leftHip != null && rightHip != null) {
            // Check if body is straight (shoulders and hips aligned)
            float shoulderHipAlignment = Math.abs(
                (leftShoulder.getPosition().y + rightShoulder.getPosition().y) / 2 -
                (leftHip.getPosition().y + rightHip.getPosition().y) / 2
            );
            
            if (shoulderHipAlignment < 40) {
                feedback.add(new FormAnalysisOverlay.FormFeedback(
                    leftShoulder.getPosition(), leftHip.getPosition(),
                    FormAnalysisOverlay.FormFeedbackType.CORRECT));
                feedback.add(new FormAnalysisOverlay.FormFeedback(
                    rightShoulder.getPosition(), rightHip.getPosition(),
                    FormAnalysisOverlay.FormFeedbackType.CORRECT));
            } else {
                feedback.add(new FormAnalysisOverlay.FormFeedback(
                    leftShoulder.getPosition(), leftHip.getPosition(),
                    FormAnalysisOverlay.FormFeedbackType.INCORRECT));
                feedback.add(new FormAnalysisOverlay.FormFeedback(
                    rightShoulder.getPosition(), rightHip.getPosition(),
                    FormAnalysisOverlay.FormFeedbackType.INCORRECT));
            }
        }
        
        return feedback;
    }
    
    /**
     * Calculates overall form score for push-ups
     * @param pose Detected pose
     * @return Form score (0.0 to 1.0)
     */
    private float calculatePushUpScore(Pose pose) {
        // Simplified scoring - in real implementation, this would be more sophisticated
        return 0.85f; // Placeholder score
    }
    
    /**
     * Calculates overall form score for squats
     * @param pose Detected pose
     * @return Form score (0.0 to 1.0)
     */
    private float calculateSquatScore(Pose pose) {
        // Simplified scoring - in real implementation, this would be more sophisticated
        return 0.78f; // Placeholder score
    }
    
    /**
     * Calculates overall form score for plank
     * @param pose Detected pose
     * @return Form score (0.0 to 1.0)
     */
    private float calculatePlankScore(Pose pose) {
        // Simplified scoring - in real implementation, this would be more sophisticated
        return 0.92f; // Placeholder score
    }
    
    /**
     * Updates the form score display
     * @param score Form score (0.0 to 1.0)
     */
    private void updateFormScore(float score) {
        int percentage = Math.round(score * 100);
        binding.formScoreText.setText(percentage + "%");
        binding.scoreCard.setVisibility(View.VISIBLE);
        
        // Update status text based on score
        if (score >= 0.8f) {
            binding.statusText.setText("Good Form!");
        } else if (score >= 0.6f) {
            binding.statusText.setText("Adjust your form");
        } else {
            binding.statusText.setText("Focus on form");
        }
    }
    
    /**
     * Toggles form analysis on/off
     */
    private void toggleAnalysis() {
        isAnalysisActive = !isAnalysisActive;
        
        if (isAnalysisActive) {
            binding.toggleAnalysisButton.setText("Stop Analysis");
            binding.statusText.setText("Form Analysis Active");
            binding.exerciseSelectionCard.setVisibility(View.GONE);
        } else {
            binding.toggleAnalysisButton.setText("Start Analysis");
            binding.statusText.setText("Position yourself in the camera view");
            binding.scoreCard.setVisibility(View.GONE);
            binding.formAnalysisOverlay.clearFeedback();
            binding.exerciseSelectionCard.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Switches between front and back camera
     */
    private void switchCamera() {
        // TODO: Implement camera switching
        Toast.makeText(requireContext(), "Camera switching not implemented yet", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Captures an image
     */
    private void captureImage() {
        // TODO: Implement image capture
        Toast.makeText(requireContext(), "Image capture not implemented yet", Toast.LENGTH_SHORT).show();
    }
    
    /**
     * Toggles camera flash
     */
    private void toggleFlash() {
        // TODO: Implement flash toggle
        Toast.makeText(requireContext(), "Flash toggle not implemented yet", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(requireContext(), "Camera permission is required for form analysis", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        if (poseDetector != null) {
            poseDetector.close();
        }
        
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        
        binding = null;
    }
}
