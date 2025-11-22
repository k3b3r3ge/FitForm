package com.fit.fitform.data.model;

public class Exercise {
    private String id;
    private String name;
    private String description;
    private String gifUrl;
    private String instructions;
    private int targetSets;
    private int targetReps;
    private int restTimeSecs;
    private boolean requiresCamera;

    public Exercise(String id, String name, String description, String gifUrl, 
                   String instructions, int targetSets, int targetReps, 
                   int restTimeSecs, boolean requiresCamera) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.gifUrl = gifUrl;
        this.instructions = instructions;
        this.targetSets = targetSets;
        this.targetReps = targetReps;
        this.restTimeSecs = restTimeSecs;
        this.requiresCamera = requiresCamera;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGifUrl() { return gifUrl; }
    public void setGifUrl(String gifUrl) { this.gifUrl = gifUrl; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public int getTargetSets() { return targetSets; }
    public void setTargetSets(int targetSets) { this.targetSets = targetSets; }

    public int getTargetReps() { return targetReps; }
    public void setTargetReps(int targetReps) { this.targetReps = targetReps; }

    public int getRestTimeSecs() { return restTimeSecs; }
    public void setRestTimeSecs(int restTimeSecs) { this.restTimeSecs = restTimeSecs; }

    public boolean isRequiresCamera() { return requiresCamera; }
    public void setRequiresCamera(boolean requiresCamera) { this.requiresCamera = requiresCamera; }
}