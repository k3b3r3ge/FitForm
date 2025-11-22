package com.fit.fitform.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Workout entity class representing a workout routine in the FitForm application.
 * Stores workout information including name, description, category, and difficulty level.
 * This entity is used with Room database for data persistence.
 */
@Entity(tableName = "workouts")
public class Workout {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;
    private long userId;
    private String name;
    private String description;
    private String category; // Strength, Cardio, Flexibility, etc.
    private int estimatedDuration; // in minutes
    private String difficulty; // Easy, Medium, Hard
    private long createdAt = System.currentTimeMillis();
    private boolean isCustom = false;

    // Default constructor
    public Workout() {}

    // Constructor with parameters
    public Workout(long userId, String name, String description, String category, 
                   int estimatedDuration, String difficulty) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.estimatedDuration = estimatedDuration;
        this.difficulty = difficulty;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }
}
