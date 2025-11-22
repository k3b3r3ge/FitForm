package com.fit.fitform.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * User entity class representing a user in the FitForm application.
 * Stores user profile information including username and email.
 * This entity is used with Room database for data persistence.
 */
@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;
    private String firebaseUid; // Firebase Authentication UID - links to Firebase Auth
    private String email;
    private String username;
    private long createdAt = System.currentTimeMillis();

    // Default constructor
    public User() {}

    // Constructor with parameters (for Firebase Auth users)
    public User(String firebaseUid, String email, String username) {
        this.firebaseUid = firebaseUid;
        this.email = email;
        this.username = username;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
