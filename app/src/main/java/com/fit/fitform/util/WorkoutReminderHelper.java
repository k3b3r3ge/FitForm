package com.fit.fitform.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.fit.fitform.receiver.WorkoutReminderReceiver;

import java.util.Calendar;

/**
 * Helper class to schedule and manage workout reminder notifications using AlarmManager.
 */
public class WorkoutReminderHelper {
    
    private static final String TAG = "WorkoutReminderHelper";
    private static final int REMINDER_REQUEST_CODE = 1001;

    /**
     * Schedules a daily workout reminder at the specified hour and minute
     * 
     * @param context Application context
     * @param hour Hour of day (0-23)
     * @param minute Minute of hour (0-59)
     */
    /**
     * Checks if the app can schedule exact alarms (Android 12+)
     * 
     * @param context Application context
     * @return true if exact alarms can be scheduled, false otherwise
     */
    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                return alarmManager.canScheduleExactAlarms();
            }
            return false;
        }
        return true; // Pre-Android 12 doesn't need permission
    }

    /**
     * Schedules a daily workout reminder at the specified hour and minute
     * 
     * @param context Application context
     * @param hour Hour of day (0-23)
     * @param minute Minute of hour (0-59)
     */
    public static void scheduleDailyReminder(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager not available");
            return;
        }

        // Create intent for WorkoutReminderReceiver
        Intent intent = new Intent(context, WorkoutReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        // Set calendar to specified time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Schedule repeating alarm
        try {
            // Check if we can schedule exact alarms on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms(context)) {
                Log.w(TAG, "Cannot schedule exact alarms - using inexact alarm as fallback");
                // Use inexact repeating alarm as fallback (doesn't require permission)
                alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
            } else {
                // Use exact repeating alarm
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                );
            }
            
            Log.d(TAG, "Daily workout reminder scheduled for " + hour + ":" + 
                  String.format("%02d", minute));
            
            // Save reminder state in SharedPreferences
            context.getSharedPreferences("workout_reminders", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("reminder_enabled", true)
                    .putInt("reminder_hour", hour)
                    .putInt("reminder_minute", minute)
                    .apply();
                    
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied to schedule alarm", e);
            // Don't crash - just log the error
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule alarm", e);
            // Catch any other exceptions to prevent crashes
        }
    }

    /**
     * Cancels the scheduled daily workout reminder
     * 
     * @param context Application context
     */
    public static void cancelDailyReminder(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "AlarmManager not available");
            return;
        }

        Intent intent = new Intent(context, WorkoutReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
        );

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d(TAG, "Daily workout reminder cancelled");
        }
        
        // Update reminder state in SharedPreferences
        context.getSharedPreferences("workout_reminders", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("reminder_enabled", false)
                .apply();
    }

    /**
     * Checks if daily reminder is currently enabled
     * 
     * @param context Application context
     * @return true if reminder is enabled, false otherwise
     */
    public static boolean isReminderEnabled(Context context) {
        return context.getSharedPreferences("workout_reminders", Context.MODE_PRIVATE)
                .getBoolean("reminder_enabled", false);
    }

    /**
     * Gets the scheduled reminder hour
     * 
     * @param context Application context
     * @return Hour of day (0-23), or -1 if not set
     */
    public static int getReminderHour(Context context) {
        return context.getSharedPreferences("workout_reminders", Context.MODE_PRIVATE)
                .getInt("reminder_hour", -1);
    }

    /**
     * Gets the scheduled reminder minute
     * 
     * @param context Application context
     * @return Minute of hour (0-59), or -1 if not set
     */
    public static int getReminderMinute(Context context) {
        return context.getSharedPreferences("workout_reminders", Context.MODE_PRIVATE)
                .getInt("reminder_minute", -1);
    }
}
