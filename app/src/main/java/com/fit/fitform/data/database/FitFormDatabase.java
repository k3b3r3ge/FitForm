package com.fit.fitform.data.database;
import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.fit.fitform.data.dao.*;
import com.fit.fitform.data.entity.*;

/**
 * Room database class for the FitForm application.
 * Manages all database entities and provides access to DAO interfaces.
 * Implements singleton pattern for database instance management.
 */
@Database(
    entities = {
        User.class,
        Workout.class,
        Exercise.class,
        WorkoutSession.class,
        ExerciseSet.class,
        FormAnalysis.class
    },
    version = 3,
    exportSchema = false
)
public abstract class FitFormDatabase extends RoomDatabase {
    
    // Abstract methods to get DAO instances
    public abstract UserDao userDao();
    public abstract WorkoutDao workoutDao();
    public abstract ExerciseDao exerciseDao();
    public abstract WorkoutSessionDao workoutSessionDao();
    public abstract ExerciseSetDao exerciseSetDao();
    public abstract FormAnalysisDao formAnalysisDao();

    // Singleton instance
    private static volatile FitFormDatabase INSTANCE;

    /**
     * Gets the database instance using singleton pattern
     * @param context Application context
     * @return FitFormDatabase instance
     */
    public static FitFormDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (FitFormDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        FitFormDatabase.class,
                        "fitform_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
