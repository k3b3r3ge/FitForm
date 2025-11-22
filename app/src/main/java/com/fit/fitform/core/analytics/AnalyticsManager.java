package com.fit.fitform.core.analytics;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Analytics Manager for tracking user events and app usage.
 * Provides centralized analytics functionality using Firebase Analytics.
 */
public class AnalyticsManager {
    
    private static AnalyticsManager instance;
    private FirebaseAnalytics firebaseAnalytics;
    
    private AnalyticsManager(Context context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }
    
    public static synchronized AnalyticsManager getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Logs user signup event
     */
    public void logSignup(String method) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, method);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle);
    }
    
    /**
     * Logs user login event
     */
    public void logLogin(String method) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.METHOD, method);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }
    
    
    /**
     * Logs workout session start
     */
    public void logWorkoutStart(String exerciseType) {
        Bundle bundle = new Bundle();
        bundle.putString("exercise_type", exerciseType);
        firebaseAnalytics.logEvent("workout_start", bundle);
    }
    
    /**
     * Logs form analysis usage
     */
    public void logFormAnalysis(String exerciseType, float score) {
        Bundle bundle = new Bundle();
        bundle.putString("exercise_type", exerciseType);
        bundle.putFloat("form_score", score);
        firebaseAnalytics.logEvent("form_analysis", bundle);
    }
    
    /**
     * Logs camera permission request
     */
    public void logCameraPermissionRequest(boolean granted) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("permission_granted", granted);
        firebaseAnalytics.logEvent("camera_permission", bundle);
    }
    
    /**
     * Sets user properties
     */
    public void setUserProperty(String name, String value) {
        firebaseAnalytics.setUserProperty(name, value);
    }
    
    /**
     * Sets user ID for analytics
     */
    public void setUserId(String userId) {
        firebaseAnalytics.setUserId(userId);
    }

    /**
     * Logs a custom event with parameters
     */
    public void logEvent(String eventName, Bundle params) {
        firebaseAnalytics.logEvent(eventName, params);
    }
}



