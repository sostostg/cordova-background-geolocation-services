package com.flybuy.cordova.location;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DetectedActivitiesIntentService extends IntentService {
    protected static final String TAG = "DetectedActivitiesIS";

    public DetectedActivitiesIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Intent localIntent = new Intent(Constants.DETECTED_ACTIVITIES_PI);

        int highestConfidence = 0;

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
        String mostLikelyActivity = "Unknown";

        // Log each activity.
        Log.w(TAG, "activities detected");

        for (DetectedActivity da: detectedActivities) {
            //Throw out the tilting and unknown activities
            if(da.getType() != DetectedActivity.TILTING) {
                if(da.getType() != DetectedActivity.UNKNOWN) {
                    Log.w(TAG, "NOT UNKNOWN OR TILTING");

                    if (highestConfidence < da.getConfidence()) {
                        highestConfidence = da.getConfidence();
                        mostLikelyActivity = Constants.getActivityString(getApplicationContext(), da.getType());

                        Log.w(TAG, "MOST LIKELY ACTIVITY: " + mostLikelyActivity + " " + highestConfidence);
                    }
                }
            }
        }

        // Broadcast the list of detected activities.
        localIntent.putExtra(Constants.ACTIVITY_EXTRA, mostLikelyActivity);
        // localIntent.putExtra(Constants.ACTIVITY_CONF_EXTRA, highestConfidence);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

}
