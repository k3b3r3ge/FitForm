package com.fit.fitform.data.database;
import android.content.Context;
import com.fit.fitform.data.entity.Exercise;
import com.fit.fitform.data.entity.Workout;
import com.fit.fitform.data.repository.WorkoutRepository;
import java.util.Arrays;
import java.util.List;

/**
 * Database initializer class for populating the FitForm database with sample data.
 * Creates pre-built workouts and exercises for users to choose from.
 * This class should be called once during app initialization or first launch.
 */
public class DatabaseInitializer {
    
    private final WorkoutRepository workoutRepository;
    
    /**
     * Constructor for DatabaseInitializer
     * @param workoutRepository WorkoutRepository instance for database operations
     */
    public DatabaseInitializer(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }
    
    /**
     * Initializes the database with sample workout data
     * @param context Application context
     */
    public static void initializeDatabase(Context context) {
        FitFormDatabase database = FitFormDatabase.getDatabase(context);
        WorkoutRepository workoutRepository = new WorkoutRepository(
            database.workoutDao(), database.exerciseDao());
        
        DatabaseInitializer initializer = new DatabaseInitializer(workoutRepository);
        initializer.createSampleWorkouts();
    }
    
    /**
     * Creates sample workouts and exercises
     */
    private void createSampleWorkouts() {
        // Create Upper Body Strength Workout
        Workout upperBodyWorkout = new Workout(
            0, // System workout (not user-specific)
            "Upper Body Strength",
            "A comprehensive upper body workout targeting chest, shoulders, and arms",
            "Strength",
            45,
            "Medium"
        );
        upperBodyWorkout.setCustom(false);
        
        long upperBodyWorkoutId = workoutRepository.insertWorkout(upperBodyWorkout);
        
        // Add exercises to Upper Body Strength Workout
        List<Exercise> upperBodyExercises = Arrays.asList(
            new Exercise(upperBodyWorkoutId, "Push-ups", 
                "Classic bodyweight exercise for chest and triceps",
                "1. Start in plank position\n2. Lower body until chest nearly touches floor\n3. Push back up to starting position",
                "Chest, Triceps, Shoulders", "None", 3, 12, 60, 1),
            new Exercise(upperBodyWorkoutId, "Diamond Push-ups",
                "Advanced push-up variation targeting triceps",
                "1. Start in plank position with hands close together forming a diamond\n2. Lower body keeping elbows close to body\n3. Push back up",
                "Triceps, Chest", "None", 3, 8, 60, 2),
            new Exercise(upperBodyWorkoutId, "Pike Push-ups",
                "Shoulder-focused push-up variation",
                "1. Start in downward dog position\n2. Lower head toward hands\n3. Push back up to starting position",
                "Shoulders, Triceps", "None", 3, 10, 60, 3),
            new Exercise(upperBodyWorkoutId, "Plank",
                "Core strengthening exercise",
                "1. Start in push-up position\n2. Hold body straight from head to heels\n3. Engage core and hold position",
                "Core, Shoulders", "None", 3, 30, 60, 4)
        );
        
        // Set form analysis capability for exercises that support it
        upperBodyExercises.get(0).setHasFormAnalysis(true);
        upperBodyExercises.get(0).setFormCheckpoints("{\"shoulder_alignment\": true, \"body_straight\": true}");
        upperBodyExercises.get(3).setHasFormAnalysis(true);
        upperBodyExercises.get(3).setFormCheckpoints("{\"body_straight\": true, \"hip_alignment\": true}");
        
        for (Exercise exercise : upperBodyExercises) {
            workoutRepository.insertExercise(exercise);
        }
        
        // Create Lower Body Strength Workout
        Workout lowerBodyWorkout = new Workout(
            0, // System workout
            "Lower Body Strength",
            "Target your legs and glutes with this comprehensive lower body workout",
            "Strength",
            40,
            "Medium"
        );
        lowerBodyWorkout.setCustom(false);
        
        long lowerBodyWorkoutId = workoutRepository.insertWorkout(lowerBodyWorkout);
        
        // Add exercises to Lower Body Strength Workout
        List<Exercise> lowerBodyExercises = Arrays.asList(
            new Exercise(lowerBodyWorkoutId, "Squats",
                "Fundamental lower body exercise",
                "1. Stand with feet shoulder-width apart\n2. Lower body as if sitting back into a chair\n3. Keep knees behind toes\n4. Return to standing position",
                "Quadriceps, Glutes, Hamstrings", "None", 4, 15, 60, 1),
            new Exercise(lowerBodyWorkoutId, "Lunges",
                "Single-leg strength and balance exercise",
                "1. Step forward with one leg\n2. Lower body until both knees are at 90 degrees\n3. Push back to starting position\n4. Repeat with other leg",
                "Quadriceps, Glutes, Hamstrings", "None", 3, 12, 60, 2),
            new Exercise(lowerBodyWorkoutId, "Glute Bridges",
                "Targeted glute strengthening exercise",
                "1. Lie on back with knees bent\n2. Lift hips up squeezing glutes\n3. Hold briefly at top\n4. Lower with control",
                "Glutes, Hamstrings", "None", 3, 15, 45, 3),
            new Exercise(lowerBodyWorkoutId, "Calf Raises",
                "Isolated calf muscle exercise",
                "1. Stand with feet hip-width apart\n2. Rise up onto toes\n3. Hold briefly at top\n4. Lower with control",
                "Calves", "None", 3, 20, 30, 4)
        );
        
        // Set form analysis capability
        lowerBodyExercises.get(0).setHasFormAnalysis(true);
        lowerBodyExercises.get(0).setFormCheckpoints("{\"knee_alignment\": true, \"depth\": true}");
        lowerBodyExercises.get(1).setHasFormAnalysis(true);
        lowerBodyExercises.get(1).setFormCheckpoints("{\"knee_alignment\": true, \"balance\": true}");
        
        for (Exercise exercise : lowerBodyExercises) {
            workoutRepository.insertExercise(exercise);
        }
        
        // Create HIIT Cardio Workout
        Workout hiitWorkout = new Workout(
            0, // System workout
            "HIIT Cardio Blast",
            "High-intensity interval training for maximum calorie burn",
            "HIIT",
            25,
            "Hard"
        );
        hiitWorkout.setCustom(false);
        
        long hiitWorkoutId = workoutRepository.insertWorkout(hiitWorkout);
        
        // Add exercises to HIIT Workout
        List<Exercise> hiitExercises = Arrays.asList(
            new Exercise(hiitWorkoutId, "Burpees",
                "Full-body high-intensity exercise",
                "1. Start standing\n2. Drop to push-up position\n3. Do a push-up\n4. Jump feet to hands\n5. Jump up with arms overhead",
                "Full Body", "None", 4, 8, 30, 1),
            new Exercise(hiitWorkoutId, "Mountain Climbers",
                "High-intensity cardio exercise",
                "1. Start in plank position\n2. Alternate bringing knees to chest\n3. Keep core engaged throughout",
                "Core, Cardio", "None", 4, 20, 30, 2),
            new Exercise(hiitWorkoutId, "Jumping Jacks",
                "Classic cardio exercise",
                "1. Start standing with arms at sides\n2. Jump feet apart while raising arms overhead\n3. Jump back to starting position",
                "Cardio, Full Body", "None", 3, 30, 30, 3),
            new Exercise(hiitWorkoutId, "High Knees",
                "Running in place with high knees",
                "1. Run in place\n2. Bring knees up to hip level\n3. Pump arms naturally",
                "Cardio, Legs", "None", 3, 30, 30, 4)
        );
        
        // Set form analysis capability
        hiitExercises.get(1).setHasFormAnalysis(true);
        hiitExercises.get(1).setFormCheckpoints("{\"body_straight\": true, \"core_engaged\": true}");
        
        for (Exercise exercise : hiitExercises) {
            workoutRepository.insertExercise(exercise);
        }
        
        // Create Flexibility & Mobility Workout
        Workout flexibilityWorkout = new Workout(
            0, // System workout
            "Flexibility & Mobility",
            "Improve flexibility and reduce muscle tension",
            "Flexibility",
            30,
            "Easy"
        );
        flexibilityWorkout.setCustom(false);
        
        long flexibilityWorkoutId = workoutRepository.insertWorkout(flexibilityWorkout);
        
        // Add exercises to Flexibility Workout
        List<Exercise> flexibilityExercises = Arrays.asList(
            new Exercise(flexibilityWorkoutId, "Cat-Cow Stretch",
                "Spinal mobility exercise",
                "1. Start on hands and knees\n2. Arch back (cow) then round spine (cat)\n3. Move slowly and breathe deeply",
                "Spine, Core", "None", 2, 10, 30, 1),
            new Exercise(flexibilityWorkoutId, "Downward Dog",
                "Full-body stretch",
                "1. Start on hands and knees\n2. Tuck toes and lift hips up\n3. Straighten legs as much as comfortable\n4. Hold position",
                "Hamstrings, Calves, Shoulders", "None", 3, 30, 45, 2),
            new Exercise(flexibilityWorkoutId, "Pigeon Pose",
                "Hip opening stretch",
                "1. Start in downward dog\n2. Bring one knee forward between hands\n3. Extend other leg back\n4. Lower to forearms for deeper stretch",
                "Hips, Glutes", "None", 2, 45, 60, 3),
            new Exercise(flexibilityWorkoutId, "Child's Pose",
                "Relaxing stretch for back and hips",
                "1. Kneel on floor\n2. Sit back on heels\n3. Extend arms forward\n4. Rest forehead on floor",
                "Back, Hips, Shoulders", "None", 2, 60, 30, 4)
        );
        
        for (Exercise exercise : flexibilityExercises) {
            workoutRepository.insertExercise(exercise);
        }
    }
}
