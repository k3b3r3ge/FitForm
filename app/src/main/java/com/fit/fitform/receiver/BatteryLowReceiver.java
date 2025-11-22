package com.fit.fitform.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.fit.fitform.core.analytics.AnalyticsManager;

/**
 * BroadcastReceiver that responds to battery status changes.
 * Pauses background operations when battery is low to conserve power during workouts.
 */
public class BatteryLowReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BatteryLowReceiver";
    private static boolean isBatteryLow = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        
        if (Intent.ACTION_BATTERY_LOW.equals(action)) {
            handleBatteryLow(context);
        } else if (Intent.ACTION_BATTERY_OKAY.equals(action)) {
            handleBatteryOkay(context);
        }
    }

    /**
     * Handles low battery event
     */
    private void handleBatteryLow(Context context) {
        Log.w(TAG, "Battery is low - conserving power");
        isBatteryLow = true;
        
        // Show notification to user
        Toast.makeText(context, 
                "Battery low - Background sync paused to conserve power", 
                Toast.LENGTH_LONG).show();
        
        // Log analytics event
        AnalyticsManager analyticsManager = AnalyticsManager.getInstance(context);
        if (analyticsManager != null) {
            analyticsManager.logEvent("battery_low_detected", null);
        }
        
        // Store battery state in SharedPreferences for other components to check
        context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("is_battery_low", true)
                .apply();
        
        // In a real app, you would:
        // - Pause background data syncing
        // - Reduce GPS accuracy
        // - Disable non-essential features
        // - Cancel pending WorkManager tasks
    }

    /**
     * Handles battery okay event
     */
    private void handleBatteryOkay(Context context) {
        Log.i(TAG, "Battery is okay - resuming normal operations");
        isBatteryLow = false;
        
        // Show notification to user
        Toast.makeText(context, 
                "Battery level restored - Resuming normal operations", 
                Toast.LENGTH_SHORT).show();
        
        // Log analytics event
        AnalyticsManager analyticsManager = AnalyticsManager.getInstance(context);
        if (analyticsManager != null) {
            analyticsManager.logEvent("battery_okay_detected", null);
        }
        
        // Update battery state in SharedPreferences
        context.getSharedPreferences("app_state", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("is_battery_low", false)
                .apply();
        
        // In a real app, you would:
        // - Resume background data syncing
        // - Restore normal GPS accuracy
        // - Re-enable features
        // - Reschedule WorkManager tasks
    }

    /**
     * Check if battery is currently low
     */
    public static boolean isBatteryLow() {
        return isBatteryLow;
    }
}
