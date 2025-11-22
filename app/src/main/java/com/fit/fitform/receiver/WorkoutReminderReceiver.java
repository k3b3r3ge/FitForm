package com.fit.fitform.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.fit.fitform.R;
import com.fit.fitform.core.analytics.AnalyticsManager;
import com.fit.fitform.ui.main.MainActivity;

/**
 * BroadcastReceiver that handles scheduled workout reminder notifications.
 * Triggered by AlarmManager to remind users about their daily workouts.
 */
public class WorkoutReminderReceiver extends BroadcastReceiver {
    
    private static final String TAG = "WorkoutReminderReceiver";
    private static final String CHANNEL_ID = "workout_reminders";
    private static final int NOTIFICATION_ID = 2001;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Workout reminder triggered");
        
        // Log analytics event
        AnalyticsManager analyticsManager = AnalyticsManager.getInstance(context);
        if (analyticsManager != null) {
            analyticsManager.logEvent("workout_reminder_shown", null);
        }
        
        // Create notification channel (required for Android O+)
        createNotificationChannel(context);
        
        // Show workout reminder notification
        showWorkoutReminderNotification(context);
    }

    /**
     * Creates notification channel for workout reminders
     */
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Workout Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Daily workout reminder notifications");
            channel.enableVibration(true);
            channel.setShowBadge(true);
            
            NotificationManager notificationManager = 
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Shows workout reminder notification
     */
    private void showWorkoutReminderNotification(Context context) {
        // Intent to open MainActivity when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_profile) // Using existing icon
                .setContentTitle("Time for your workout! 💪")
                .setContentText("Stay consistent with your fitness goals")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Don't break your streak! A quick workout session will help you stay on track with your fitness journey."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true) // Dismiss when tapped
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 500, 200, 500}); // Vibration pattern

        // Show notification
        NotificationManager notificationManager = 
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Workout reminder notification displayed");
        }
    }
}
