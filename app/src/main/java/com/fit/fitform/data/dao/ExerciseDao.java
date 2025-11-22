package com.fit.fitform.data.dao;
import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.fit.fitform.data.entity.Exercise;
import java.util.List;

/**
 * Data Access Object (DAO) interface for Exercise entity operations.
 * Provides methods for exercise management within workouts including form analysis capabilities.
 * Uses Room database for data persistence with LiveData for reactive UI updates.
 */
@Dao
public interface ExerciseDao {
    
    /**
     * Retrieves all exercises for a specific workout, ordered by their sequence
     * @param workoutId Workout's unique identifier
     * @return LiveData list of exercises for reactive UI updates
     */
    @Query("SELECT * FROM exercises WHERE workoutId = :workoutId ORDER BY `order` ASC")
    LiveData<List<Exercise>> getExercisesByWorkout(long workoutId);
    
    /**
     * Retrieves an exercise by its unique ID
     * @param exerciseId Exercise's unique identifier
     * @return Exercise object if found, null otherwise
     */
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    Exercise getExerciseById(long exerciseId);
    
    /**
     * Retrieves all exercises that support form analysis
     * @return LiveData list of exercises with form analysis capabilities
     */
    @Query("SELECT * FROM exercises WHERE hasFormAnalysis = 1")
    LiveData<List<Exercise>> getExercisesWithFormAnalysis();
    
    /**
     * Inserts a new exercise into the database
     * @param exercise Exercise object to insert
     * @return The ID of the newly inserted exercise
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertExercise(Exercise exercise);
    
    /**
     * Updates an existing exercise in the database
     * @param exercise Exercise object with updated information
     */
    @Update
    void updateExercise(Exercise exercise);
    
    /**
     * Deletes an exercise from the database
     * @param exercise Exercise object to delete
     */
    @Delete
    void deleteExercise(Exercise exercise);
    
    /**
     * Deletes all exercises for a specific workout
     * @param workoutId Workout's unique identifier
     */
    @Query("DELETE FROM exercises WHERE workoutId = :workoutId")
    void deleteExercisesByWorkout(long workoutId);
    
    /**
     * Retrieves all exercises from the database
     * @return LiveData list of all exercises for reactive UI updates
     */
    @Query("SELECT * FROM exercises")
    LiveData<List<Exercise>> getAllExercises();
}
