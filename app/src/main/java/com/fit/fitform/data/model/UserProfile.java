package com.fit.fitform.data.model;

import java.util.HashMap;
import java.util.Map;

/**
 * UserProfile model for Firestore database.
 * Represents user profile information stored in Firebase Firestore.
 */
public class UserProfile {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private int age;
    private float weight; // in kg
    private float height; // in cm
    private String fitnessLevel; // Beginner, Intermediate, Advanced
    private long createdAt;

    // Default constructor required for Firestore
    public UserProfile() {
    }

    // Constructor with parameters
    public UserProfile(String userId, String email, String firstName, String lastName,
                      int age, float weight, float height, String fitnessLevel) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.weight = weight;
        this.height = height;
        this.fitnessLevel = fitnessLevel;
        this.createdAt = System.currentTimeMillis();
    }

    // Convert to Map for Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("email", email);
        map.put("firstName", firstName);
        map.put("lastName", lastName);
        map.put("age", age);
        map.put("weight", weight);
        map.put("height", height);
        map.put("fitnessLevel", fitnessLevel);
        map.put("createdAt", createdAt);
        return map;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getFitnessLevel() {
        return fitnessLevel;
    }

    public void setFitnessLevel(String fitnessLevel) {
        this.fitnessLevel = fitnessLevel;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}

