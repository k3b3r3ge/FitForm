package com.fit.fitform.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.fit.fitform.data.entity.ExerciseSet;
import java.util.List;

/**
 * Data Access Object (DAO) interface for ExerciseSet entity operations.
 * Provides methods for tracking individual exercise sets including reps, weight, and form scores.
 * Uses Room database for data persistence with LiveData for reactive UI updates.
 */
@Dao
public interface ExerciseSetDao {
    
    /**
     * Retrieves all exercise sets for a specific workout session, ordered by completion time
     * @param sessionId Session's unique identifier
     * @return LiveData list of exercise sets for reactive UI updates
     */
    @Query("SELECT * FROM exercise_sets WHERE sessionId = :sessionId ORDER BY completedAt ASC")
    LiveData<List<ExerciseSet>> getSetsBySession(long sessionId);
    
    /**
     * Retrieves all exercise sets for a specific exercise within a session
     * @param exerciseId Exercise's unique identifier
     * @param sessionId Session's unique identifier
     * @return LiveData list of exercise sets for reactive UI updates
     */
    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :exerciseId AND sessionId = :sessionId")
    LiveData<List<ExerciseSet>> getSetsByExerciseAndSession(long exerciseId, long sessionId);
    
    /**
     * Retrieves an exercise set by its unique ID
     * @param setId Exercise set's unique identifier
     * @return ExerciseSet object if found, null otherwise
     */
    @Query("SELECT * FROM exercise_sets WHERE id = :setId")
    ExerciseSet getSetById(long setId);
    
    /**
     * Inserts a new exercise set into the database
     * @param exerciseSet ExerciseSet object to insert
     * @return The ID of the newly inserted exercise set
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSet(ExerciseSet exerciseSet);
    
    /**
     * Updates an existing exercise set in the database
     * @param exerciseSet ExerciseSet object with updated information
     */
    @Update
    void updateSet(ExerciseSet exerciseSet);
    
    /**
     * Deletes an exercise set from the database
     * @param exerciseSet ExerciseSet object to delete
     */
    @Delete
    void deleteSet(ExerciseSet exerciseSet);
    
    /**
     * Deletes all exercise sets for a specific workout session
     * @param sessionId Session's unique identifier
     */
    @Query("DELETE FROM exercise_sets WHERE sessionId = :sessionId")
    void deleteSetsBySession(long sessionId);
    
    /**
     * Calculates the average form score for a specific exercise
     * @param exerciseId Exercise's unique identifier
     * @return Average form score (0.0 to 1.0) or null if no scores available
     */
    @Query("SELECT AVG(formScore) FROM exercise_sets WHERE exerciseId = :exerciseId AND formScore IS NOT NULL")
    Float getAverageFormScore(long exerciseId);
}
