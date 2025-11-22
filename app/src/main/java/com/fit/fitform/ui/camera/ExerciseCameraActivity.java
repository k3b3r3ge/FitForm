package com.fit.fitform.ui.camera;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.fit.fitform.R;
import com.fit.fitform.data.database.FitFormDatabase;
import com.fit.fitform.data.entity.WorkoutSession;
import com.fit.fitform.core.analytics.AnalyticsManager;
import com.fit.fitform.service.WorkoutTimerService;
import com.fit.fitform.util.GraphicOverlay;
import com.fit.fitform.util.PoseAnalyzer;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ExerciseCameraActivity extends AppCompatActivity {
    private static final String TAG = "ExerciseCameraActivity";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 2001;

    private PreviewView previewView;
    private GraphicOverlay graphicOverlay;
    private TextView feedbackTextView;
    private TextView timerTextView;
    private Button finishWorkoutButton;
    private ExerciseCameraViewModel viewModel;
    private ProcessCameraProvider cameraProvider;
    private Camera camera;
    private ImageAnalysis imageAnalysis;
    private PoseAnalyzer poseAnalyzer;
    private AnalyticsManager analyticsManager;
    private boolean isAnalysisActive = true;
    private FitFormDatabase db;
    private long activeSessionId = -1L;
    private long activeSessionStartMs = 0L;
    
    // BroadcastReceiver for timer updates from WorkoutTimerService
    private BroadcastReceiver timerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WorkoutTimerService.ACTION_TIMER_UPDATE.equals(intent.getAction())) {
                long elapsedSeconds = intent.getLongExtra(WorkoutTimerService.EXTRA_ELAPSED_TIME, 0);
                updateTimerDisplay(elapsedSeconds);
            }
        }
    };
    private long workoutId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_camera);

        // Initialize views
        previewView = findViewById(R.id.previewView);
        graphicOverlay = findViewById(R.id.graphicOverlay);
        feedbackTextView = findViewById(R.id.feedbackTextView);
        timerTextView = findViewById(R.id.timerTextView);
        finishWorkoutButton = findViewById(R.id.finishWorkoutButton);
        
        // Register BroadcastReceiver for timer updates
        IntentFilter filter = new IntentFilter(WorkoutTimerService.ACTION_TIMER_UPDATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(timerReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(timerReceiver, filter);
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ExerciseCameraViewModel.class);

        // Initialize Analytics
        analyticsManager = AnalyticsManager.getInstance(this);
        db = FitFormDatabase.getDatabase(getApplicationContext());
        
        // Read selected exercise from intent (if any)
        String exerciseType = getIntent().getStringExtra("exercise_type");
        workoutId = getIntent().getLongExtra("workout_id", -1L);
        if (exerciseType != null) {
            viewModel.setSelectedExercise(exerciseType);
            Log.d(TAG, "Starting " + exerciseType + " capture");
            // Track workout start
            analyticsManager.logWorkoutStart(exerciseType);
        } else {
            exerciseType = "PUSHUP"; // Default
            viewModel.setSelectedExercise(exerciseType);
            analyticsManager.logWorkoutStart(exerciseType);
        }

        // Initialize PoseAnalyzer with GraphicOverlay and feedback TextView
        poseAnalyzer = new PoseAnalyzer(graphicOverlay, feedbackTextView, exerciseType);

        // Start a WorkoutSession record
        startWorkoutSession();
        
        // Start WorkoutTimerService
        Intent serviceIntent = new Intent(this, WorkoutTimerService.class);
        serviceIntent.setAction(WorkoutTimerService.ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // Finish action
        finishWorkoutButton.setOnClickListener(v -> finishWorkoutSessionAndExit());

        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null) return;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageAnalysis = new ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .build();

        // Use PoseAnalyzer which handles pose detection and visualization
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), poseAnalyzer);

        androidx.camera.core.CameraSelector cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA;

        try {
            cameraProvider.unbindAll();
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            Log.d(TAG, "Camera bound successfully with pose analysis");
        } catch (Exception e) {
            Log.e(TAG, "Error binding camera use cases", e);
            Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void startWorkoutSession() {
        activeSessionStartMs = System.currentTimeMillis();
        final long userId = getSharedPreferences("user_session", 0).getLong("user_id", -1);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                WorkoutSession session = new WorkoutSession(userId, workoutId, activeSessionStartMs);
                long id = db.workoutSessionDao().insertSession(session);
                activeSessionId = id;
                Log.d(TAG, "Workout session started id=" + id);
            } catch (Exception e) {
                Log.e(TAG, "Failed to start workout session", e);
            }
        });
    }

    private void finishWorkoutSessionAndExit() {
        // Stop WorkoutTimerService
        Intent serviceIntent = new Intent(this, WorkoutTimerService.class);
        serviceIntent.setAction(WorkoutTimerService.ACTION_STOP);
        startService(serviceIntent);
        
        final long endMs = System.currentTimeMillis();
        final int durationMin = (int) Math.max(1, (endMs - activeSessionStartMs) / 60000);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (activeSessionId > 0) {
                    WorkoutSession session = db.workoutSessionDao().getSessionById(activeSessionId);
                    if (session != null) {
                        session.setEndTime(endMs);
                        session.setTotalDuration(durationMin);
                        session.setCaloriesBurned(Math.max(5, durationMin * 5));
                        session.setCompleted(true);
                        db.workoutSessionDao().updateSession(session);
                        // Persist an ExerciseSet summary for this session
                        try {
                            com.fit.fitform.data.entity.ExerciseSet set = new com.fit.fitform.data.entity.ExerciseSet();
                            set.setSessionId(activeSessionId);
                            set.setExerciseId(0); // Unknown mapping; can be resolved later
                            set.setSetNumber(1);
                            String ex = viewModel.getSelectedExercise();
                            if (ex != null) {
                                String name = ex.toUpperCase();
                                if (name.contains("PUSH")) {
                                    set.setReps(poseAnalyzer.getCorrectReps());
                                    set.setDuration(null);
                                } else if (name.contains("SQUAT")) {
                                    set.setReps(poseAnalyzer.getCorrectReps());
                                    set.setDuration(null);
                                } else if (name.contains("PLANK")) {
                                    set.setReps(0);
                                    set.setDuration(poseAnalyzer.getPlankSeconds());
                                }
                            }
                            db.exerciseSetDao().insertSet(set);
                        } catch (Exception ignored) {}
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to complete workout session", e);
            }
            runOnUiThread(this::finish);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            // Track camera permission request
            if (analyticsManager != null) {
                analyticsManager.logCameraPermissionRequest(granted);
            }
            if (granted) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Updates the timer display with formatted time
     */
    private void updateTimerDisplay(long totalSeconds) {
        if (timerTextView != null) {
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;
            String timeString = String.format("%02d:%02d", minutes, seconds);
            timerTextView.setText(timeString);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Unregister BroadcastReceiver
        try {
            unregisterReceiver(timerReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver was not registered
        }
        
        isAnalysisActive = false;
        if (imageAnalysis != null) {
            imageAnalysis.clearAnalyzer();
        }
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (graphicOverlay != null) {
            graphicOverlay.clear();
        }
    }
}