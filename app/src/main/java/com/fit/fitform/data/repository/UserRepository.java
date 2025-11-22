package com.fit.fitform.data.repository;

import androidx.lifecycle.LiveData;
import com.fit.fitform.data.dao.UserDao;
import com.fit.fitform.data.entity.User;
import java.util.List;

/**
 * Repository class for User entity operations.
 * Provides a clean API for user-related database operations and acts as a single source of truth.
 * Handles user authentication, registration, and profile management.
 */
public class UserRepository {
    
    private final UserDao userDao;
    
    /**
     * Constructor for UserRepository
     * @param userDao UserDao instance for database operations
     */
    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
    }
    
    /**
     * Retrieves a user by Firebase UID
     * @param firebaseUid Firebase Authentication UID
     * @return User object if found, null otherwise
     */
    public User getUserByFirebaseUid(String firebaseUid) {
        return userDao.getUserByFirebaseUid(firebaseUid);
    }
    
    /**
     * Retrieves a user by email address
     * @param email User's email address
     * @return User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }
    
    /**
     * Retrieves a user by their unique ID
     * @param userId User's unique identifier
     * @return User object if found, null otherwise
     */
    public User getUserById(long userId) {
        return userDao.getUserById(userId);
    }
    
    /**
     * Inserts a new user into the database
     * @param user User object to insert
     * @return The ID of the newly inserted user
     */
    public long insertUser(User user) {
        return userDao.insertUser(user);
    }
    
    /**
     * Updates an existing user in the database
     * @param user User object with updated information
     */
    public void updateUser(User user) {
        userDao.updateUser(user);
    }
    
    /**
     * Deletes a user from the database
     * @param user User object to delete
     */
    public void deleteUser(User user) {
        userDao.deleteUser(user);
    }
    
    /**
     * Retrieves all users from the database
     * @return LiveData list of all users for reactive UI updates
     */
    public LiveData<List<User>> getAllUsers() {
        return userDao.getAllUsers();
    }
}
