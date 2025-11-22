package com.fit.fitform.ui.camera;

import androidx.lifecycle.ViewModel;

public class ExerciseCameraViewModel extends ViewModel {
    private String selectedExercise;

    public String getSelectedExercise() {
        return selectedExercise;
    }

    public void setSelectedExercise(String selectedExercise) {
        this.selectedExercise = selectedExercise;
    }
}