package com.fit.fitform.data.entity;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * FormAnalysis entity class representing ML-based form analysis results for exercise sets.
 * Stores detailed feedback, scores, and recommendations from the AI form analysis system.
 * This entity is used with Room database for data persistence.
 */
@Entity(tableName = "form_analysis")
public class FormAnalysis {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;
    private long exerciseSetId;
    private long timestamp;
    private float overallScore; // 0.0 to 1.0
    private String feedback; // JSON string with detailed feedback
    private String keyPointScores; // JSON string with scores for each key point
    private String recommendations = null; // Suggestions for improvement

    // Default constructor
    public FormAnalysis() {}

    // Constructor with parameters
    public FormAnalysis(long exerciseSetId, long timestamp, float overallScore, 
                       String feedback, String keyPointScores) {
        this.exerciseSetId = exerciseSetId;
        this.timestamp = timestamp;
        this.overallScore = overallScore;
        this.feedback = feedback;
        this.keyPointScores = keyPointScores;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExerciseSetId() {
        return exerciseSetId;
    }

    public void setExerciseSetId(long exerciseSetId) {
        this.exerciseSetId = exerciseSetId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(float overallScore) {
        this.overallScore = overallScore;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getKeyPointScores() {
        return keyPointScores;
    }

    public void setKeyPointScores(String keyPointScores) {
        this.keyPointScores = keyPointScores;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }
}
