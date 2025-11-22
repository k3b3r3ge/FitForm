package com.fit.fitform.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

/**
 * A graphic to draw the detected pose landmarks and connecting lines.
 * This version allows for changing the paint color for real-time feedback.
 */
public class PoseGraphic extends GraphicOverlay.Graphic {

    private static final float DOT_RADIUS = 8.0f;
    private static final float STROKE_WIDTH = 10.0f;

    private final Pose pose;
    private final Paint jointPaint;
    private final Paint bodyPaint;
    private final Paint armPaint;
    private final Paint legPaint;

    public PoseGraphic(GraphicOverlay overlay, Pose pose) {
        super(overlay);
        this.pose = pose;

        jointPaint = new Paint();
        jointPaint.setColor(Color.YELLOW);
        jointPaint.setStyle(Paint.Style.FILL);

        // Initialize paints with default color (GREEN for correct form)
        bodyPaint = new Paint();
        bodyPaint.setColor(Color.GREEN);
        bodyPaint.setStrokeWidth(STROKE_WIDTH);
        bodyPaint.setStyle(Paint.Style.STROKE);

        armPaint = new Paint(bodyPaint);
        legPaint = new Paint(bodyPaint);
    }

    /**
     * Sets the color of the lines used to draw the body, arms, or legs.
     * @param color The color to set (e.g., Color.RED for incorrect form).
     * @param part "body", "arms", "legs", or "all".
     */
    public void setLineColor(int color, String part) {
        switch (part.toLowerCase()) {
            case "body":
                bodyPaint.setColor(color);
                break;
            case "arms":
                armPaint.setColor(color);
                break;
            case "legs":
                legPaint.setColor(color);
                break;
            case "all":
                bodyPaint.setColor(color);
                armPaint.setColor(color);
                legPaint.setColor(color);
                break;
        }
    }


    @Override
    public void draw(Canvas canvas) {
        List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
        if (landmarks.isEmpty()) {
            return;
        }

        // Draw all the joints
        for (PoseLandmark landmark : landmarks) {
            if (landmark.getInFrameLikelihood() > 0.5f) { // Only draw visible landmarks
                drawPoint(canvas, landmark, jointPaint);
            }
        }

        // Define connections and draw lines
        PoseLandmark leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER);
        PoseLandmark rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER);
        PoseLandmark leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW);
        PoseLandmark rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW);
        PoseLandmark leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST);
        PoseLandmark rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST);
        PoseLandmark leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP);
        PoseLandmark rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP);
        PoseLandmark leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE);
        PoseLandmark rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE);
        PoseLandmark leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE);
        PoseLandmark rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE);

        // Body
        drawLine(canvas, leftShoulder, rightShoulder, bodyPaint);
        drawLine(canvas, leftHip, rightHip, bodyPaint);
        drawLine(canvas, leftShoulder, leftHip, bodyPaint);
        drawLine(canvas, rightShoulder, rightHip, bodyPaint);

        // Arms
        drawLine(canvas, leftShoulder, leftElbow, armPaint);
        drawLine(canvas, leftElbow, leftWrist, armPaint);
        drawLine(canvas, rightShoulder, rightElbow, armPaint);
        drawLine(canvas, rightElbow, rightWrist, armPaint);

        // Legs
        drawLine(canvas, leftHip, leftKnee, legPaint);
        drawLine(canvas, leftKnee, leftAnkle, legPaint);
        drawLine(canvas, rightHip, rightKnee, legPaint);
        drawLine(canvas, rightKnee, rightAnkle, legPaint);
    }

    void drawPoint(Canvas canvas, PoseLandmark landmark, Paint paint) {
        if (landmark == null) return;
        canvas.drawCircle(translateX(landmark.getPosition().x), translateY(landmark.getPosition().y), DOT_RADIUS, paint);
    }

    void drawLine(Canvas canvas, PoseLandmark start, PoseLandmark end, Paint paint) {
        if (start == null || end == null || start.getInFrameLikelihood() < 0.5f || end.getInFrameLikelihood() < 0.5f) {
            return;
        }
        canvas.drawLine(
                translateX(start.getPosition().x),
                translateY(start.getPosition().y),
                translateX(end.getPosition().x),
                translateY(end.getPosition().y),
                paint
        );
    }
}
