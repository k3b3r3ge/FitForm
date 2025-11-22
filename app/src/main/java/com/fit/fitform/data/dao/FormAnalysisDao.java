package com.fit.fitform.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.fit.fitform.data.entity.FormAnalysis;
import java.util.List;

/**
 * Data Access Object (DAO) interface for FormAnalysis entity operations.
 * Provides methods for storing and retrieving ML-based form analysis results.
 * Uses Room database for data persistence with LiveData for reactive UI updates.
 */
@Dao
public interface FormAnalysisDao {
    
    /**
     * Retrieves all form analysis records for a specific exercise set, ordered by timestamp
     * @param exerciseSetId Exercise set's unique identifier
     * @return LiveData list of form analysis records for reactive UI updates
     */
    @Query("SELECT * FROM form_analysis WHERE exerciseSetId = :exerciseSetId ORDER BY timestamp ASC")
    LiveData<List<FormAnalysis>> getAnalysisByExerciseSet(long exerciseSetId);
    
    /**
     * Retrieves a form analysis record by its unique ID
     * @param analysisId Form analysis record's unique identifier
     * @return FormAnalysis object if found, null otherwise
     */
    @Query("SELECT * FROM form_analysis WHERE id = :analysisId")
    FormAnalysis getAnalysisById(long analysisId);
    
    /**
     * Inserts a new form analysis record into the database
     * @param analysis FormAnalysis object to insert
     * @return The ID of the newly inserted form analysis record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAnalysis(FormAnalysis analysis);
    
    /**
     * Updates an existing form analysis record in the database
     * @param analysis FormAnalysis object with updated information
     */
    @Update
    void updateAnalysis(FormAnalysis analysis);
    
    /**
     * Deletes a form analysis record from the database
     * @param analysis FormAnalysis object to delete
     */
    @Delete
    void deleteAnalysis(FormAnalysis analysis);
    
    /**
     * Calculates the average overall score for a specific exercise set
     * @param exerciseSetId Exercise set's unique identifier
     * @return Average overall score (0.0 to 1.0) or null if no scores available
     */
    @Query("SELECT AVG(overallScore) FROM form_analysis WHERE exerciseSetId = :exerciseSetId")
    Float getAverageScoreForSet(long exerciseSetId);
    
    /**
     * Deletes all form analysis records for a specific exercise set
     * @param exerciseSetId Exercise set's unique identifier
     */
    @Query("DELETE FROM form_analysis WHERE exerciseSetId = :exerciseSetId")
    void deleteAnalysisByExerciseSet(long exerciseSetId);
}
