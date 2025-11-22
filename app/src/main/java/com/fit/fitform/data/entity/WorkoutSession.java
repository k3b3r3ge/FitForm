package com.fit.fitform.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * WorkoutSession entity class representing a workout session instance.
 * Tracks when a user starts and completes a workout, including duration and calories burned.
 * This entity is used with Room database for data persistence.
 */
@Entity(tableName = "workout_sessions")
public class WorkoutSession {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;
    private long userId;
    private long workoutId;
    private long startTime;
    private Long endTime = null;
    private Integer totalDuration = null; // in minutes
    private Integer caloriesBurned = null;
    private boolean isCompleted = false;
    private String notes = null;

    // Default constructor
    public WorkoutSession() {}

    // Constructor with parameters
    public WorkoutSession(long userId, long workoutId, long startTime) {
        this.userId = userId;
        this.workoutId = workoutId;
        this.startTime = startTime;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(long workoutId) {
        this.workoutId = workoutId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Integer getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
    }

    public Integer getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(Integer caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
