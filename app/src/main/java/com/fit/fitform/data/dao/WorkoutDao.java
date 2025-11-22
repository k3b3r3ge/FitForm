package com.fit.fitform.data.dao;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.fit.fitform.data.entity.Workout;
import java.util.List;

/**
 * Data Access Object (DAO) interface for Workout entity operations.
 * Provides methods for workout management including creation, retrieval, and categorization.
 * Uses Room database for data persistence with LiveData for reactive UI updates.
 */
@Dao
public interface WorkoutDao {
    
    /**
     * Retrieves all workouts for a specific user
     * @param userId User's unique identifier
     * @return LiveData list of user's workouts for reactive UI updates
     */
    @Query("SELECT * FROM workouts WHERE userId = :userId")
    LiveData<List<Workout>> getWorkoutsByUser(long userId);
    
    /**
     * Retrieves a workout by its unique ID
     * @param workoutId Workout's unique identifier
     * @return Workout object if found, null otherwise
     */
    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    Workout getWorkoutById(long workoutId);
    
    /**
     * Retrieves workouts by category
     * @param category Workout category (e.g., Strength, Cardio, Flexibility)
     * @return LiveData list of workouts in the specified category
     */
    @Query("SELECT * FROM workouts WHERE category = :category")
    LiveData<List<Workout>> getWorkoutsByCategory(String category);
    
    /**
     * Retrieves all pre-built workouts (non-custom)
     * @return LiveData list of pre-built workouts
     */
    @Query("SELECT * FROM workouts WHERE isCustom = 0")
    LiveData<List<Workout>> getPreBuiltWorkouts();
    
    /**
     * Inserts a new workout into the database
     * @param workout Workout object to insert
     * @return The ID of the newly inserted workout
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertWorkout(Workout workout);
    
    /**
     * Updates an existing workout in the database
     * @param workout Workout object with updated information
     */
    @Update
    void updateWorkout(Workout workout);
    
    /**
     * Deletes a workout from the database
     * @param workout Workout object to delete
     */
    @Delete
    void deleteWorkout(Workout workout);
    
    /**
     * Retrieves all workouts from the database
     * @return LiveData list of all workouts for reactive UI updates
     */
    @Query("SELECT * FROM workouts")
    LiveData<List<Workout>> getAllWorkouts();
}
