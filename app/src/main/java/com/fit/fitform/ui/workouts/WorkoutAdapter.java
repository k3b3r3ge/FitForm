package com.fit.fitform.ui.workouts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.fit.fitform.R;
import com.fit.fitform.data.entity.Workout;
import java.util.List;

/**
 * RecyclerView adapter for displaying workout items in a list.
 * Handles workout display, click events, and provides options for editing and deleting workouts.
 * Uses Material Design cards for consistent UI presentation.
 */
public class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder> {
    
    private List<Workout> workouts;
    private OnWorkoutClickListener clickListener;
    
    /**
     * Interface for handling workout click events
     */
    public interface OnWorkoutClickListener {
        void onWorkoutClick(Workout workout);
        void onWorkoutEdit(Workout workout);
        void onWorkoutDelete(Workout workout);
    }
    
    /**
     * Constructor for WorkoutAdapter
     * @param workouts List of workouts to display
     * @param clickListener Click listener for workout events
     */
    public WorkoutAdapter(List<Workout> workouts, OnWorkoutClickListener clickListener) {
        this.workouts = workouts;
        this.clickListener = clickListener;
    }
    
    @NonNull
    @Override
    public WorkoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_workout, parent, false);
        return new WorkoutViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WorkoutViewHolder holder, int position) {
        Workout workout = workouts.get(position);
        holder.bind(workout, clickListener);
    }
    
    @Override
    public int getItemCount() {
        return workouts.size();
    }
    
    /**
     * ViewHolder class for workout items
     */
    public static class WorkoutViewHolder extends RecyclerView.ViewHolder {
        
        private TextView workoutName;
        private TextView workoutDescription;
        private TextView workoutCategory;
        private TextView workoutDuration;
        private TextView workoutDifficulty;
        private ImageView categoryIcon;
        private ImageView editButton;
        private ImageView deleteButton;
        
        public WorkoutViewHolder(@NonNull View itemView) {
            super(itemView);
            
            workoutName = itemView.findViewById(R.id.workoutName);
            workoutDescription = itemView.findViewById(R.id.workoutDescription);
            workoutCategory = itemView.findViewById(R.id.workoutCategory);
            workoutDuration = itemView.findViewById(R.id.workoutDuration);
            workoutDifficulty = itemView.findViewById(R.id.workoutDifficulty);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
        
        /**
         * Binds workout data to the view holder
         * @param workout Workout object to display
         * @param clickListener Click listener for events
         */
        public void bind(Workout workout, OnWorkoutClickListener clickListener) {
            workoutName.setText(workout.getName());
            workoutDescription.setText(workout.getDescription());
            workoutCategory.setText(workout.getCategory());
            workoutDuration.setText(workout.getEstimatedDuration() + " min");
            workoutDifficulty.setText(workout.getDifficulty());
            
            // Set category icon based on workout category
            setCategoryIcon(workout.getCategory());
            
            // Set click listeners
            itemView.setOnClickListener(v -> clickListener.onWorkoutClick(workout));
            editButton.setOnClickListener(v -> clickListener.onWorkoutEdit(workout));
            deleteButton.setOnClickListener(v -> clickListener.onWorkoutDelete(workout));
        }
        
        /**
         * Sets the appropriate icon for the workout category
         * @param category Workout category
         */
        private void setCategoryIcon(String category) {
            switch (category.toLowerCase()) {
                case "strength":
                    categoryIcon.setImageResource(R.drawable.ic_fitness_center);
                    break;
                case "cardio":
                    categoryIcon.setImageResource(R.drawable.ic_trending_up);
                    break;
                case "hiit":
                    categoryIcon.setImageResource(R.drawable.ic_flash_on);
                    break;
                case "flexibility":
                    categoryIcon.setImageResource(R.drawable.ic_accessibility);
                    break;
                default:
                    categoryIcon.setImageResource(R.drawable.ic_workout);
                    break;
            }
        }
    }
}
