package com.fit.fitform.data.dao;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.fit.fitform.data.entity.User;
import java.util.List;

/**
 * Data Access Object (DAO) interface for User entity operations.
 * Provides methods for user authentication, registration, and profile management.
 * Uses Room database for data persistence with LiveData for reactive UI updates.
 */
@Dao
public interface UserDao {
    
    /**
     * Retrieves a user by Firebase UID
     * @param firebaseUid Firebase Authentication UID
     * @return User object if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE firebaseUid = :firebaseUid")
    User getUserByFirebaseUid(String firebaseUid);
    
    /**
     * Retrieves a user by email address
     * @param email User's email address
     * @return User object if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE email = :email")
    User getUserByEmail(String email);
    
    /**
     * Retrieves a user by their unique ID
     * @param userId User's unique identifier
     * @return User object if found, null otherwise
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    User getUserById(long userId);
    
    /**
     * Inserts a new user into the database
     * @param user User object to insert
     * @return The ID of the newly inserted user
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User user);
    
    /**
     * Updates an existing user in the database
     * @param user User object with updated information
     */
    @Update
    void updateUser(User user);
    
    /**
     * Deletes a user from the database
     * @param user User object to delete
     */
    @Delete
    void deleteUser(User user);
    
    /**
     * Retrieves all users from the database
     * @return LiveData list of all users for reactive UI updates
     */
    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsers();
}
