package com.fit.fitform.data.entity;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Exercise entity class representing an individual exercise within a workout.
 * Stores exercise details including instructions, target muscles, and form analysis capabilities.
 * This entity is used with Room database for data persistence.
 */
@Entity(tableName = "exercises")
public class Exercise implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id = 0;
    private long workoutId;
    private String name;
    private String description;
    private String instructions;
    private String targetMuscles; // Comma-separated list
    private String equipment; // None, Dumbbells, Barbell, etc.
    private int sets;
    private int reps;
    private int restTime; // in seconds
    private int order; // Order in workout
    private boolean hasFormAnalysis = false; // Whether this exercise supports ML form analysis
    private String formCheckpoints; // JSON string of key points to check
    private String gifUrl; // URL or resource path for exercise demonstration GIF

    // Default constructor
    public Exercise() {}

    // Constructor with parameters
    public Exercise(long workoutId, String name, String description, String instructions,
                   String targetMuscles, String equipment, int sets, int reps, int restTime, int order) {
        this.workoutId = workoutId;
        this.name = name;
        this.description = description;
        this.instructions = instructions;
        this.targetMuscles = targetMuscles;
        this.equipment = equipment;
        this.sets = sets;
        this.reps = reps;
        this.restTime = restTime;
        this.order = order;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWorkoutId() {
        return workoutId;
    }

    public void setWorkoutId(long workoutId) {
        this.workoutId = workoutId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getTargetMuscles() {
        return targetMuscles;
    }

    public void setTargetMuscles(String targetMuscles) {
        this.targetMuscles = targetMuscles;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public int getSets() {
        return sets;
    }

    public void setSets(int sets) {
        this.sets = sets;
    }

    public int getReps() {
        return reps;
    }

    public void setReps(int reps) {
        this.reps = reps;
    }

    public int getRestTime() {
        return restTime;
    }

    public void setRestTime(int restTime) {
        this.restTime = restTime;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isHasFormAnalysis() {
        return hasFormAnalysis;
    }

    public void setHasFormAnalysis(boolean hasFormAnalysis) {
        this.hasFormAnalysis = hasFormAnalysis;
    }

    public String getFormCheckpoints() {
        return formCheckpoints;
    }

    public void setFormCheckpoints(String formCheckpoints) {
        this.formCheckpoints = formCheckpoints;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
    }

    public boolean isRequiresCamera() {
        return hasFormAnalysis;
    }

    // Parcelable implementation
    protected Exercise(Parcel in) {
        id = in.readLong();
        workoutId = in.readLong();
        name = in.readString();
        description = in.readString();
        instructions = in.readString();
        targetMuscles = in.readString();
        equipment = in.readString();
        sets = in.readInt();
        reps = in.readInt();
        restTime = in.readInt();
        order = in.readInt();
        hasFormAnalysis = in.readByte() != 0;
        formCheckpoints = in.readString();
        gifUrl = in.readString();
    }

    public static final Creator<Exercise> CREATOR = new Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(workoutId);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(instructions);
        dest.writeString(targetMuscles);
        dest.writeString(equipment);
        dest.writeInt(sets);
        dest.writeInt(reps);
        dest.writeInt(restTime);
        dest.writeInt(order);
        dest.writeByte((byte) (hasFormAnalysis ? 1 : 0));
        dest.writeString(formCheckpoints);
        dest.writeString(gifUrl);
    }
}
