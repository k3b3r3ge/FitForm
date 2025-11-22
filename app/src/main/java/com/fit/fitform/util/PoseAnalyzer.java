package com.fit.fitform.util;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

public class PoseAnalyzer implements ImageAnalysis.Analyzer {

    private static final String TAG = "PoseAnalyzer";

    private final PoseDetector poseDetector;
    private final GraphicOverlay graphicOverlay;
    private final TextView feedbackTextView;
    private final String exerciseType;

    // Repetition counters
    private int repCounter = 0;
    private int correctRepCounter = 0;

    // State for exercise counters (e.g., "UP", "DOWN")
    private String exerciseState = "UP";

    // State/error flags per rep
    private boolean currentRepHasError = false;

    // Pushup constants
    private static final double PUSHUP_DOWN_ANGLE_THRESHOLD = 90.0;
    private static final double PUSHUP_UP_ANGLE_THRESHOLD = 160.0;

    // Squat constants
    private static final double SQUAT_DOWN_ANGLE_THRESHOLD = 100.0;
    private static final double SQUAT_UP_ANGLE_THRESHOLD = 170.0;

    // General form constants
    private static final double BODY_STRAIGHT_ANGLE_THRESHOLD = 160.0;

    // Plank timing
    private long lastTimestampMs = System.currentTimeMillis();
    private long plankHeldMillis = 0L;

    public PoseAnalyzer(GraphicOverlay graphicOverlay, TextView feedbackTextView, String exerciseType) {
        this.graphicOverlay = graphicOverlay;
        this.feedbackTextView = feedbackTextView;
        this.exerciseType = exerciseType != null ? exerciseType.toUpperCase() : "PUSHUP";

        // Use AccuratePoseDetectorOptions for better pose detection
        AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder()
                .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
                .build();
        this.poseDetector = PoseDetection.getClient(options);
    }

    @Override
    @SuppressLint("UnsafeOptInUsageError")
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            Task<Pose> result = poseDetector.process(image)
                    .addOnSuccessListener(pose -> {
                        // Clear previous graphics
                        graphicOverlay.clear();
                        // Set image source info for proper coordinate transformation
                        graphicOverlay.setImageSourceInfo(image.getWidth(), image.getHeight(), true);

                        // Create pose graphic for visualization
                        PoseGraphic poseGraphic = new PoseGraphic(graphicOverlay, pose);

                        // Route to the correct analysis function based on exercise type
                        // This will set colors (green for correct, red for wrong)
                        switch (exerciseType) {
                            case "PUSHUP":
                            case "PUSH-UP":
                            case "PUSH_UP":
                                analyzePushup(pose, poseGraphic);
                                break;
                            case "SQUAT":
                                analyzeSquat(pose, poseGraphic);
                                break;
                            case "PLANK":
                                analyzePlank(pose, poseGraphic, System.currentTimeMillis());
                                break;
                            default:
                                // Default to pushup analysis
                                analyzePushup(pose, poseGraphic);
                                break;
                        }

                        // Add the pose graphic to overlay (this will trigger drawing)
                        graphicOverlay.add(poseGraphic);
                        // Force overlay to redraw
                        graphicOverlay.postInvalidate();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Pose detection failed", e);
                        updateFeedback("Pose detection error", Color.RED);
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        }
    }

    private void analyzePushup(Pose pose, PoseGraphic poseGraphic) {
        // Default all parts to green (correct form)
        poseGraphic.setLineColor(Color.GREEN, "all");
        
        PoseLandmark leftShoulder = getLandmark(pose, PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftElbow = getLandmark(pose, PoseLandmark.LEFT_ELBOW);
        PoseLandmark leftWrist = getLandmark(pose, PoseLandmark.LEFT_WRIST);
        PoseLandmark rightShoulder = getLandmark(pose, PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark rightElbow = getLandmark(pose, PoseLandmark.RIGHT_ELBOW);
        PoseLandmark rightWrist = getLandmark(pose, PoseLandmark.RIGHT_WRIST);
        PoseLandmark leftHip = getLandmark(pose, PoseLandmark.LEFT_HIP);
        PoseLandmark leftAnkle = getLandmark(pose, PoseLandmark.LEFT_ANKLE);

        if (allLandmarksVisible(leftShoulder, leftElbow, leftWrist, leftHip, leftAnkle)) {
            double elbowAngle = getAngle(leftShoulder, leftElbow, leftWrist);
            double bodyAngle = getAngle(leftShoulder, leftHip, leftAnkle);
            
            // Check right arm angle too if available
            double rightElbowAngle = 180.0;
            if (allLandmarksVisible(rightShoulder, rightElbow, rightWrist)) {
                rightElbowAngle = getAngle(rightShoulder, rightElbow, rightWrist);
            }

            boolean isBodyStraight = bodyAngle >= BODY_STRAIGHT_ANGLE_THRESHOLD;
            boolean isElbowAngleGood = elbowAngle > PUSHUP_DOWN_ANGLE_THRESHOLD && elbowAngle < PUSHUP_UP_ANGLE_THRESHOLD;

            if (!isBodyStraight) {
                // Body not straight - mark body as red
                poseGraphic.setLineColor(Color.RED, "body");
                updateFeedback("Keep your body straight!", Color.RED);
                currentRepHasError = true;
            } else if (!isElbowAngleGood && elbowAngle < PUSHUP_DOWN_ANGLE_THRESHOLD) {
                // Arms too bent - mark arms as red
                poseGraphic.setLineColor(Color.RED, "arms");
                updateFeedback("Lower your body more", Color.RED);
                currentRepHasError = true;
            } else {
                // Form is good - keep green
                poseGraphic.setLineColor(Color.GREEN, "all");
                if (elbowAngle > PUSHUP_UP_ANGLE_THRESHOLD) {
                    if (exerciseState.equals("DOWN")) {
                        repCounter++;
                        if (!currentRepHasError) {
                            correctRepCounter++;
                        }
                        currentRepHasError = false;
                    }
                    exerciseState = "UP";
                } else if (elbowAngle < PUSHUP_DOWN_ANGLE_THRESHOLD) {
                    exerciseState = "DOWN";
                }
                updateRepCount();
            }
        } else {
            updateFeedback("Make sure your whole body is visible", Color.YELLOW);
            currentRepHasError = true;
        }
    }

    private void analyzeSquat(Pose pose, PoseGraphic poseGraphic) {
        // Default all parts to green (correct form)
        poseGraphic.setLineColor(Color.GREEN, "all");
        
        PoseLandmark leftShoulder = getLandmark(pose, PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftHip = getLandmark(pose, PoseLandmark.LEFT_HIP);
        PoseLandmark leftKnee = getLandmark(pose, PoseLandmark.LEFT_KNEE);
        PoseLandmark leftAnkle = getLandmark(pose, PoseLandmark.LEFT_ANKLE);
        PoseLandmark rightHip = getLandmark(pose, PoseLandmark.RIGHT_HIP);
        PoseLandmark rightKnee = getLandmark(pose, PoseLandmark.RIGHT_KNEE);
        PoseLandmark rightAnkle = getLandmark(pose, PoseLandmark.RIGHT_ANKLE);

        if (allLandmarksVisible(leftHip, leftKnee, leftAnkle)) {
            double kneeAngle = getAngle(leftHip, leftKnee, leftAnkle);
            
            // Check back straightness
            boolean isBackStraight = true;
            if (allLandmarksVisible(leftShoulder, leftHip, leftKnee)) {
                double backAngle = getAngle(leftShoulder, leftHip, leftKnee);
                isBackStraight = backAngle >= 160.0; // Back should be relatively straight
            }

            if (!isBackStraight) {
                // Back not straight - mark body as red
                poseGraphic.setLineColor(Color.RED, "body");
                updateFeedback("Keep your back straight!", Color.RED);
                currentRepHasError = true;
            } else if (kneeAngle < 70.0) {
                // Knees too bent (knee going too far forward) - mark legs as red
                poseGraphic.setLineColor(Color.RED, "legs");
                updateFeedback("Don't let your knees go too far forward", Color.RED);
                currentRepHasError = true;
            } else {
                // Form is good - keep green
                poseGraphic.setLineColor(Color.GREEN, "all");
                if (kneeAngle > SQUAT_UP_ANGLE_THRESHOLD) {
                    if (exerciseState.equals("DOWN")) {
                        repCounter++;
                        if (!currentRepHasError) {
                            correctRepCounter++;
                        }
                        currentRepHasError = false;
                    }
                    exerciseState = "UP";
                } else if (kneeAngle < SQUAT_DOWN_ANGLE_THRESHOLD) {
                    exerciseState = "DOWN";
                }
                updateRepCount();
            }
        } else {
            updateFeedback("Make sure your legs are visible", Color.YELLOW);
            currentRepHasError = true;
        }
    }

    private void analyzePlank(Pose pose, PoseGraphic poseGraphic, long nowMs) {
        // Default all parts to green (correct form)
        poseGraphic.setLineColor(Color.GREEN, "all");
        
        PoseLandmark leftShoulder = getLandmark(pose, PoseLandmark.LEFT_SHOULDER);
        PoseLandmark leftHip = getLandmark(pose, PoseLandmark.LEFT_HIP);
        PoseLandmark leftAnkle = getLandmark(pose, PoseLandmark.LEFT_ANKLE);

        if (allLandmarksVisible(leftShoulder, leftHip, leftAnkle)) {
            double bodyAngle = getAngle(leftShoulder, leftHip, leftAnkle);

            boolean isBodyStraight = bodyAngle >= BODY_STRAIGHT_ANGLE_THRESHOLD;

            if (isBodyStraight) {
                // Form is good - keep green
                poseGraphic.setLineColor(Color.GREEN, "all");
                long dt = nowMs - lastTimestampMs;
                if (dt > 0 && dt < 1000) {
                    plankHeldMillis += dt;
                }
                updateFeedback("Hold a straight line", Color.GREEN);
                // In a real app, you would start or continue a timer here
            } else {
                // Body not straight - mark body as red
                poseGraphic.setLineColor(Color.RED, "body");
                updateFeedback("Straighten your back!", Color.RED);
                // And pause the timer
            }
        } else {
            updateFeedback("Make sure your whole body is visible", Color.YELLOW);
        }
        lastTimestampMs = nowMs;
    }

    // Helper method to get a landmark, using the right side as a fallback
    private PoseLandmark getLandmark(Pose pose, int landmarkType) {
        PoseLandmark landmark = pose.getPoseLandmark(landmarkType);
        if (landmark == null || landmark.getInFrameLikelihood() < 0.6f) {
            // If left landmark is not visible, try the corresponding right landmark
            int rightLandmarkType = landmarkType + 1; // e.g., LEFT_SHOULDER (11) -> RIGHT_SHOULDER (12)
            return pose.getPoseLandmark(rightLandmarkType);
        }
        return landmark;
    }

    // Helper to check if all provided landmarks are not null
    private boolean allLandmarksVisible(PoseLandmark... landmarks) {
        for (PoseLandmark landmark : landmarks) {
            if (landmark == null) return false;
        }
        return true;
    }

    private double getAngle(PoseLandmark first, PoseLandmark mid, PoseLandmark last) {
        double angle = Math.toDegrees(
                Math.atan2(last.getPosition().y - mid.getPosition().y, last.getPosition().x - mid.getPosition().x) -
                        Math.atan2(first.getPosition().y - mid.getPosition().y, first.getPosition().x - mid.getPosition().x)
        );
        angle = Math.abs(angle);
        if (angle > 180) {
            angle = 360 - angle;
        }
        return angle;
    }

    private void updateRepCount() {
        new Handler(Looper.getMainLooper()).post(() -> {
            feedbackTextView.setText(String.format("%s: %d (good: %d)", exerciseType, repCounter, correctRepCounter));
        });
    }

    private void updateFeedback(String text, int color) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Log.d(TAG, "Feedback: " + text);
            if (color == Color.RED || color == Color.YELLOW) {
                // Show immediate feedback for corrections
                feedbackTextView.setText(text);
                feedbackTextView.setTextColor(color);
            } else if (!feedbackTextView.getText().toString().contains(":") && color == Color.WHITE) {
                // Show instructive feedback if no rep count is visible
                feedbackTextView.setText(text);
                feedbackTextView.setTextColor(color);
            }
        });
    }

    public int getCorrectReps() {
        return correctRepCounter;
    }

    public int getPlankSeconds() {
        return (int) (plankHeldMillis / 1000L);
    }
}
