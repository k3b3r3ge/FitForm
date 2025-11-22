package com.fit.fitform.data.repository;

import androidx.lifecycle.LiveData;
import com.fit.fitform.data.dao.WorkoutDao;
import com.fit.fitform.data.dao.ExerciseDao;
import com.fit.fitform.data.entity.Workout;
import com.fit.fitform.data.entity.Exercise;
import java.util.List;

/**
 * Repository class for Workout and Exercise entity operations.
 * Provides a clean API for workout and exercise-related database operations.
 * Handles workout creation, management, and exercise tracking.
 */
public class WorkoutRepository {
    
    private final WorkoutDao workoutDao;
    private final ExerciseDao exerciseDao;
    
    /**
     * Constructor for WorkoutRepository
     * @param workoutDao WorkoutDao instance for workout operations
     * @param exerciseDao ExerciseDao instance for exercise operations
     */
    public WorkoutRepository(WorkoutDao workoutDao, ExerciseDao exerciseDao) {
        this.workoutDao = workoutDao;
        this.exerciseDao = exerciseDao;
    }
    
    // Workout operations
    
    /**
     * Retrieves all workouts for a specific user
     * @param userId User's unique identifier
     * @return LiveData list of user's workouts for reactive UI updates
     */
    public LiveData<List<Workout>> getWorkoutsByUser(long userId) {
        return workoutDao.getWorkoutsByUser(userId);
    }
    
    /**
     * Retrieves a workout by its unique ID
     * @param workoutId Workout's unique identifier
     * @return Workout object if found, null otherwise
     */
    public Workout getWorkoutById(long workoutId) {
        return workoutDao.getWorkoutById(workoutId);
    }
    
    /**
     * Retrieves workouts by category
     * @param category Workout category (e.g., Strength, Cardio, Flexibility)
     * @return LiveData list of workouts in the specified category
     */
    public LiveData<List<Workout>> getWorkoutsByCategory(String category) {
        return workoutDao.getWorkoutsByCategory(category);
    }
    
    /**
     * Retrieves all pre-built workouts (non-custom)
     * @return LiveData list of pre-built workouts
     */
    public LiveData<List<Workout>> getPreBuiltWorkouts() {
        return workoutDao.getPreBuiltWorkouts();
    }
    
    /**
     * Inserts a new workout into the database
     * @param workout Workout object to insert
     * @return The ID of the newly inserted workout
     */
    public long insertWorkout(Workout workout) {
        return workoutDao.insertWorkout(workout);
    }
    
    /**
     * Updates an existing workout in the database
     * @param workout Workout object with updated information
     */
    public void updateWorkout(Workout workout) {
        workoutDao.updateWorkout(workout);
    }
    
    /**
     * Deletes a workout from the database
     * @param workout Workout object to delete
     */
    public void deleteWorkout(Workout workout) {
        workoutDao.deleteWorkout(workout);
    }
    
    // Exercise operations
    
    /**
     * Retrieves all exercises for a specific workout, ordered by their sequence
     * @param workoutId Workout's unique identifier
     * @return LiveData list of exercises for reactive UI updates
     */
    public LiveData<List<Exercise>> getExercisesByWorkout(long workoutId) {
        return exerciseDao.getExercisesByWorkout(workoutId);
    }
    
    /**
     * Retrieves an exercise by its unique ID
     * @param exerciseId Exercise's unique identifier
     * @return Exercise object if found, null otherwise
     */
    public Exercise getExerciseById(long exerciseId) {
        return exerciseDao.getExerciseById(exerciseId);
    }
    
    /**
     * Retrieves all exercises that support form analysis
     * @return LiveData list of exercises with form analysis capabilities
     */
    public LiveData<List<Exercise>> getExercisesWithFormAnalysis() {
        return exerciseDao.getExercisesWithFormAnalysis();
    }
    
    /**
     * Inserts a new exercise into the database
     * @param exercise Exercise object to insert
     * @return The ID of the newly inserted exercise
     */
    public long insertExercise(Exercise exercise) {
        return exerciseDao.insertExercise(exercise);
    }
    
    /**
     * Updates an existing exercise in the database
     * @param exercise Exercise object with updated information
     */
    public void updateExercise(Exercise exercise) {
        exerciseDao.updateExercise(exercise);
    }
    
    /**
     * Deletes an exercise from the database
     * @param exercise Exercise object to delete
     */
    public void deleteExercise(Exercise exercise) {
        exerciseDao.deleteExercise(exercise);
    }
    
    /**
     * Deletes all exercises for a specific workout
     * @param workoutId Workout's unique identifier
     */
    public void deleteExercisesByWorkout(long workoutId) {
        exerciseDao.deleteExercisesByWorkout(workoutId);
    }
}
