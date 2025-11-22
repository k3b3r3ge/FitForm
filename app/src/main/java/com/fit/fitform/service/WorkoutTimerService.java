package com.fit.fitform.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.fit.fitform.R;
import com.fit.fitform.ui.main.MainActivity;

/**
 * Foreground Service for tracking workout session duration.
 * Displays a persistent notification with elapsed time and continues running
 * even when the app is in the background.
 */
public class WorkoutTimerService extends Service {

    private static final String TAG = "WorkoutTimerService";
    private static final String CHANNEL_ID = "workout_timer_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    // Broadcast action for timer updates
    public static final String ACTION_TIMER_UPDATE = "com.fit.fitform.TIMER_UPDATE";
    public static final String EXTRA_ELAPSED_TIME = "elapsed_time";
    
    // Service actions
    public static final String ACTION_START = "com.fit.fitform.ACTION_START";
    public static final String ACTION_PAUSE = "com.fit.fitform.ACTION_PAUSE";
    public static final String ACTION_RESUME = "com.fit.fitform.ACTION_RESUME";
    public static final String ACTION_STOP = "com.fit.fitform.ACTION_STOP";
    
    private Handler handler;
    private Runnable timerRunnable;
    private long elapsedTimeSeconds = 0;
    private boolean isRunning = false;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();
        
        handler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    elapsedTimeSeconds++;
                    updateNotification();
                    broadcastTimeUpdate();
                    handler.postDelayed(this, 1000); // Update every second
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_START:
                    startTimer();
                    break;
                case ACTION_PAUSE:
                    pauseTimer();
                    break;
                case ACTION_RESUME:
                    resumeTimer();
                    break;
                case ACTION_STOP:
                    stopTimer();
                    stopSelf();
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // This is a started service, not bound
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacks(timerRunnable);
        }
        isRunning = false;
    }

    /**
     * Creates notification channel for Android O and above
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Workout Timer",
                    NotificationManager.IMPORTANCE_LOW // Low importance = no sound
            );
            channel.setDescription("Shows workout session duration");
            channel.setShowBadge(false);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Starts the timer and shows foreground notification
     */
    private void startTimer() {
        elapsedTimeSeconds = 0;
        isRunning = true;
        
        // Start as foreground service with notification
        startForeground(NOTIFICATION_ID, buildNotification());
        
        // Start timer updates
        handler.post(timerRunnable);
    }

    /**
     * Pauses the timer
     */
    private void pauseTimer() {
        isRunning = false;
        updateNotification();
    }

    /**
     * Resumes the timer
     */
    private void resumeTimer() {
        isRunning = true;
        handler.post(timerRunnable);
    }

    /**
     * Stops the timer and removes notification
     */
    private void stopTimer() {
        isRunning = false;
        elapsedTimeSeconds = 0;
        if (handler != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }

    /**
     * Builds the notification to display
     */
    private Notification buildNotification() {
        // Intent to open app when notification is tapped
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );

        String timeString = formatTime(elapsedTimeSeconds);
        String contentText = isRunning ? "Workout in progress: " + timeString : "Workout paused: " + timeString;

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("FitForm Workout")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_profile) // Using existing icon
                .setContentIntent(pendingIntent)
                .setOngoing(true) // Cannot be dismissed by user
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    /**
     * Updates the notification with current time
     */
    private void updateNotification() {
        Notification notification = buildNotification();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Broadcasts the current elapsed time to any registered receivers
     */
    private void broadcastTimeUpdate() {
        Intent intent = new Intent(ACTION_TIMER_UPDATE);
        intent.putExtra(EXTRA_ELAPSED_TIME, elapsedTimeSeconds);
        sendBroadcast(intent);
    }

    /**
     * Formats seconds into MM:SS format
     */
    private String formatTime(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
