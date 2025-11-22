package com.fit.fitform.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.fit.fitform.data.entity.WorkoutSession;
import java.util.List;

/**
 * Data Access Object (DAO) interface for WorkoutSession entity operations.
 * Provides methods for tracking workout sessions including start/end times and completion status.
 * Uses Room database for data persistence with LiveData for reactive UI updates.
 */
@Dao
public interface WorkoutSessionDao {
    
    /**
     * Retrieves all workout sessions for a specific user, ordered by start time (most recent first)
     * @param userId User's unique identifier
     * @return LiveData list of user's workout sessions for reactive UI updates
     */
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY startTime DESC")
    LiveData<List<WorkoutSession>> getSessionsByUser(long userId);
    
    /**
     * Retrieves a workout session by its unique ID
     * @param sessionId Session's unique identifier
     * @return WorkoutSession object if found, null otherwise
     */
    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    WorkoutSession getSessionById(long sessionId);
    
    /**
     * Retrieves recent completed workout sessions for a user
     * @param userId User's unique identifier
     * @param limit Maximum number of sessions to retrieve
     * @return LiveData list of recent completed sessions
     */
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND isCompleted = 1 ORDER BY startTime DESC LIMIT :limit")
    LiveData<List<WorkoutSession>> getRecentCompletedSessions(long userId, int limit);
    
    /**
     * Inserts a new workout session into the database
     * @param session WorkoutSession object to insert
     * @return The ID of the newly inserted session
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSession(WorkoutSession session);
    
    /**
     * Updates an existing workout session in the database
     * @param session WorkoutSession object with updated information
     */
    @Update
    void updateSession(WorkoutSession session);
    
    /**
     * Deletes a workout session from the database
     * @param session WorkoutSession object to delete
     */
    @Delete
    void deleteSession(WorkoutSession session);
    
    /**
     * Counts the number of completed workouts for a user
     * @param userId User's unique identifier
     * @return Number of completed workout sessions
     */
    @Query("SELECT COUNT(*) FROM workout_sessions WHERE userId = :userId AND isCompleted = 1")
    int getCompletedWorkoutCount(long userId);
    
    /**
     * Retrieves the currently active (incomplete) workout session for a user
     * @param userId User's unique identifier
     * @return Active WorkoutSession if found, null otherwise
     */
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND isCompleted = 0")
    WorkoutSession getActiveSession(long userId);
}
