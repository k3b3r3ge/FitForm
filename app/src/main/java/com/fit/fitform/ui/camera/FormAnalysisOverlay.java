package com.fit.fitform.ui.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom View for displaying form analysis feedback overlay on camera preview.
 * Draws red lines for incorrect form and green lines for correct form.
 * Provides visual feedback for exercise form analysis using ML Kit pose detection.
 */
public class FormAnalysisOverlay extends View {
    
    private Paint correctPaint;
    private Paint incorrectPaint;
    private Paint neutralPaint;
    private List<FormFeedback> feedbackList;
    
    /**
     * Constructor for FormAnalysisOverlay
     * @param context Application context
     */
    public FormAnalysisOverlay(Context context) {
        super(context);
        init();
    }
    
    /**
     * Constructor for FormAnalysisOverlay with attributes
     * @param context Application context
     * @param attrs View attributes
     */
    public FormAnalysisOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    /**
     * Constructor for FormAnalysisOverlay with attributes and style
     * @param context Application context
     * @param attrs View attributes
     * @param defStyleAttr Default style attribute
     */
    public FormAnalysisOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    /**
     * Initializes the overlay with paint objects and feedback list
     */
    private void init() {
        // Initialize paint objects for different feedback types
        correctPaint = new Paint();
        correctPaint.setColor(Color.GREEN);
        correctPaint.setStrokeWidth(8f);
        correctPaint.setStyle(Paint.Style.STROKE);
        correctPaint.setAntiAlias(true);
        
        incorrectPaint = new Paint();
        incorrectPaint.setColor(Color.RED);
        incorrectPaint.setStrokeWidth(8f);
        incorrectPaint.setStyle(Paint.Style.STROKE);
        incorrectPaint.setAntiAlias(true);
        
        neutralPaint = new Paint();
        neutralPaint.setColor(Color.YELLOW);
        neutralPaint.setStrokeWidth(6f);
        neutralPaint.setStyle(Paint.Style.STROKE);
        neutralPaint.setAntiAlias(true);
        
        feedbackList = new ArrayList<>();
    }
    
    /**
     * Updates the form feedback data and triggers redraw
     * @param feedback List of form feedback items to display
     */
    public void updateFeedback(List<FormFeedback> feedback) {
        this.feedbackList = feedback != null ? new ArrayList<>(feedback) : new ArrayList<>();
        invalidate(); // Trigger redraw
    }
    
    /**
     * Clears all form feedback and triggers redraw
     */
    public void clearFeedback() {
        this.feedbackList.clear();
        invalidate(); // Trigger redraw
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw all feedback items
        for (FormFeedback feedback : feedbackList) {
            Paint paint = getPaintForFeedback(feedback.getType());
            canvas.drawLine(
                feedback.getStartPoint().x, feedback.getStartPoint().y,
                feedback.getEndPoint().x, feedback.getEndPoint().y,
                paint
            );
        }
    }
    
    /**
     * Gets the appropriate paint object based on feedback type
     * @param type Type of form feedback
     * @return Paint object for drawing
     */
    private Paint getPaintForFeedback(FormFeedbackType type) {
        switch (type) {
            case CORRECT:
                return correctPaint;
            case INCORRECT:
                return incorrectPaint;
            case NEUTRAL:
            default:
                return neutralPaint;
        }
    }
    
    /**
     * Enum for different types of form feedback
     */
    public enum FormFeedbackType {
        CORRECT,    // Green lines for correct form
        INCORRECT,  // Red lines for incorrect form
        NEUTRAL     // Yellow lines for neutral/attention needed
    }
    
    /**
     * Data class representing a single form feedback item
     */
    public static class FormFeedback {
        private PointF startPoint;
        private PointF endPoint;
        private FormFeedbackType type;
        
        /**
         * Constructor for FormFeedback
         * @param startPoint Starting point of the feedback line
         * @param endPoint Ending point of the feedback line
         * @param type Type of feedback (correct, incorrect, neutral)
         */
        public FormFeedback(PointF startPoint, PointF endPoint, FormFeedbackType type) {
            this.startPoint = startPoint;
            this.endPoint = endPoint;
            this.type = type;
        }
        
        // Getters
        public PointF getStartPoint() { return startPoint; }
        public PointF getEndPoint() { return endPoint; }
        public FormFeedbackType getType() { return type; }
    }
}
