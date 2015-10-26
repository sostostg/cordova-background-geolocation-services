package com.flybuy.cordova.location;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.location.DetectedActivity;

public class Constants {
	private Constants() {

	}

	private static final String P_NAME = "com.flybuy.cordova.location.";
    
    private static final String STOP_RECORDING  = P_NAME + "STOP_RECORDING";
    private static final String START_RECORDING = P_NAME + "START_RECORDING";
    private static final String CHANGE_AGGRESSIVE = P_NAME + "CHANGE_AGGRESSIVE";
    private static final String STOP_GEOFENCES = P_NAME + "STOP_GEOFENCES";
    private static final String CALLBACK_LOCATION_UPDATE = P_NAME + "CALLBACK_LOCATION_UPDATE";

    public static final String DETECTED_ACTIVITIES_PI = P_NAME + "DETECTED_ACTIVITIES_PI";
    public static final String ACTIVITY_EXTRA = P_NAME + "ACTIVITY_EXTRA";

     public static String getActivityString(Context context, int detectedActivityType) {
        Resources resources = context.getResources();
        switch(detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.TILTING:
                return "Titling";
            case DetectedActivity.UNKNOWN:
                return "Unknown";
            case DetectedActivity.WALKING:
                return "Walking";
            default:
                return "Unknown";
        }
    }
}