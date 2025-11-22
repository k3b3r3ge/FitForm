package com.fit.fitform.data.entity;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * ExerciseSet entity class representing a completed set of an exercise during a workout session.
 * Tracks reps, weight, duration, and form score for each set performed.
 * This entity is used with Room database for data persistence.
 */
@Entity(tableName = "exercise_sets")
public class ExerciseSet {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;
    private long sessionId;
    private long exerciseId;
    private int setNumber;
    private int reps;
    private Float weight = null; // in kg
    private Integer duration = null; // in seconds for time-based exercises
    private Float formScore = null; // 0.0 to 1.0, calculated by ML analysis
    private long completedAt = System.currentTimeMillis();

    // Default constructor
    public ExerciseSet() {}

    // Constructor with parameters
    public ExerciseSet(long sessionId, long exerciseId, int setNumber, int reps) {
        this.sessionId = sessionId;
        this.exerciseId = exerciseId;
        this.setNumber = setNumber;
        this.reps = reps;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public int getSetNumber() {
        return setNumber;
    }

    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Float getFormScore() {
        return formScore;
    }

    public void setFormScore(Float formScore) {
        this.formScore = formScore;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }
}
