package com.flybuy.cordova.location;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import android.app.PendingIntent;
import android.widget.Toast;

public class BackgroundLocationServicesPlugin extends CordovaPlugin {
    private static final String TAG = "BackgroundLocationServicesPlugin";
    private static final String PLUGIN_VERSION = "1.0";
    
    public static final String ACTION_START = "start";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_CONFIGURE = "configure";
    public static final String ACTION_SET_CONFIG = "setConfig";
    public static final String ACTION_AGGRESSIVE_TRACKING = "startAggressiveTracking";
    public static final String ACTION_GET_VERSION = "getVersion";
    public static final String ACTION_REGISTER_FOR_LOCATION_UPDATES = "registerForLocationUpdates";
    
    private Intent updateServiceIntent;
    private Intent geofenceServiceIntent;
    
    private Boolean isEnabled = false;
    
    private String desiredAccuracy = "1000";
    
    private String interval = "300000";
    private String fastestInterval = "60000";
    private String aggressiveInterval = "4000";
    
    private String distanceFilter = "30";
    private String isDebugging = "false";
    private String notificationTitle = "Location Tracking";
    private String notificationText = "ENABLED";
    private String stopOnTerminate = "false";
    private String activityType = "Automotive";

    //Things I want to remove
    private String url;
    private String params;
    private String headers;
    
    private JSONArray fences = null;
    
    private static CallbackContext locationUpdateCallback = null;
    
    private BroadcastReceiver receiver = null;
    private BroadcastReceiver detectedActivityReceiver = null;
    private GoogleApiClient mGoogleActivityClient;
    
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) {
        
        Activity activity = this.cordova.getActivity();
        
        Boolean result = false;
        // updateServiceIntent = new Intent(activity, BackgroundLocationUpdateService.class);
        
        if (ACTION_START.equalsIgnoreCase(action) && !isEnabled) {
            result = true;
            // if (params == null || headers == null|| url == null) {
            //     callbackContext.error("Call configure before calling start");
            // } else {
            //     callbackContext.success();
            //     updateServiceIntent.putExtra("desiredAccuracy", desiredAccuracy);
            //     updateServiceIntent.putExtra("distanceFilter", distanceFilter);
            //     updateServiceIntent.putExtra("desiredAccuracy", desiredAccuracy);
            //     updateServiceIntent.putExtra("isDebugging", isDebugging);
            //     updateServiceIntent.putExtra("notificationTitle", notificationTitle);
            //     updateServiceIntent.putExtra("notificationText", notificationText);
            //     updateServiceIntent.putExtra("interval", interval);
            //     updateServiceIntent.putExtra("fastestInterval", fastestInterval);
            //     updateServiceIntent.putExtra("aggressiveInterval", aggressiveInterval);

            //     //URL / PARAMS
            //     updateServiceIntent.putExtra("url", url);
            //     updateServiceIntent.putExtra("params", params);
            //     updateServiceIntent.putExtra("headers", headers);

            //     activity.startService(updateServiceIntent);

            //     createLocationUpdateReceiver();
            //     webView.getContext().registerReceiver(this.receiver, new IntentFilter(CALLBACK_LOCATION_UPDATE));

            //     isEnabled = true;
            // }

            if(mGoogleActivityClient == null) {
                buildActivityClient();
            }

            this.detectedActivityReceiver = new ActivityDetectionReceiver();
            // LocalBroadcastManager.getInstance(webView.getContext()).registerReceiver(this.detectedActivityReceiver,
            //     new IntentFilter(Constants.DETECTED_ACTIVITIES_PI));

            enableActivity();

        } else if (ACTION_STOP.equalsIgnoreCase(action)) {
            isEnabled = false;
            result = true;
            // activity.stopService(updateServiceIntent);
            callbackContext.success();
            
            // destroyLocationUpdateReceiver();
        } else if (ACTION_CONFIGURE.equalsIgnoreCase(action)) {
            result = true;
            try {
                // [distanceFilter, desiredAccuracy, interval, fastestInterval, aggressiveInterval, debug, notificationTitle, notificationText, activityType, fences, url, params, headers]
                //  0               1                2         3                4                   5      6                   7                8              9
                this.distanceFilter = data.getString(0);
                this.desiredAccuracy = data.getString(1);
                this.interval = data.getString(2);
                this.fastestInterval = data.getString(3);
                this.aggressiveInterval = data.getString(4);
                this.isDebugging = data.getString(5);
                this.notificationTitle = data.getString(6);
                this.notificationText = data.getString(7);
                this.activityType = data.getString(8);

                this.url = data.getString(10);
                Log.d(TAG, "URL" + this.url);
                this.params = data.getString(11);
                this.headers = data.getString(12);
                
            } catch (JSONException e) {
                Log.d(TAG, "Json Exception" + e);
                callbackContext.error("authToken/url required as parameters: " + e.getMessage());
            }
        } else if (ACTION_SET_CONFIG.equalsIgnoreCase(action)) {
            result = true;
            // TODO reconfigure Service
            callbackContext.success();
        } else if(ACTION_GET_VERSION.equalsIgnoreCase(action)) {
            result = true;
            callbackContext.success(PLUGIN_VERSION);
        } else if(ACTION_REGISTER_FOR_LOCATION_UPDATES.equalsIgnoreCase(action)) {
            result = true;
            // if(debug()) {
            //     Log.w(TAG, "WARNING: Anroid does not support callbacks yet. Use the HTTP configuration");
            // }
            //Register the funcition for repeated location update
            locationUpdateCallback = callbackContext;
            // callbackContext.error("Anroid does not support callbacks yet. Use the HTTP configuration");
        } else if(ACTION_AGGRESSIVE_TRACKING.equalsIgnoreCase(action)) {
            result = true;
            if(isEnabled) {
                // this.cordova.getActivity().sendBroadcast(new Intent(CHANGE_AGGRESSIVE));
                callbackContext.success();
            } else {
                callbackContext.error("Tracking not enabled, need to start tracking before starting aggressive tracking");
            }
        }

        return result;
    }
    
    public Boolean debug() {
        if(Boolean.parseBoolean(isDebugging)) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public void onPause(boolean multitasking) {
        if(debug()) {
            Log.d(TAG, "- locationUpdateReceiver Paused (starting recording = " + String.valueOf(isEnabled) + ")");
        }
        if (isEnabled) {
            // Activity activity = this.cordova.getActivity();
            // activity.sendBroadcast(new Intent(START_RECORDING));
        }
    }
    
    @Override
    public void onResume(boolean multitasking) {
        if(debug()) {
            Log.d(TAG, "- locationUpdateReceiver Resumed (stopping recording)" + String.valueOf(isEnabled));
        }
        if (isEnabled) {
            // Activity activity = this.cordova.getActivity();
            // activity.sendBroadcast(new Intent(STOP_RECORDING));
        }
    }

    protected void enableActivity() {
        Log.w(TAG, "Enabling Activity Results");
        isEnabled = true;
        mGoogleActivityClient.connect();
    }


    public static class ActivityDetectionReceiver extends BroadcastReceiver {
        public ActivityDetectionReceiver() {}
        
        @Override
        public void onReceive(Context context, final Intent intent) {
            String updatedActivity =
                    intent.getStringExtra(Constants.ACTIVITY_EXTRA);
            // int confidence = intent.getIntExtra(Constants.ACTIVITY_CONF_EXTRA, 0);

            // if(updatedActivity != "unknown") {

            //     mActivityText.setText(updatedActivity + " " + confidence + "%");
            //     Log.w(TAG, updatedActivity);
            //     if (updatedActivity.equals("Still") && isUpdatingLocation) {
            //         stopUpdatingLocation();
            //     } else if (!updatedActivity.equals("Still") && !isUpdatingLocation) {
            //         startUpdatingLocation();
            //     }
            // }
            Log.w(TAG, "RECEIVED DETECTED ACTIVITY " + updatedActivity);
            Toast.makeText(context, "We recieveived a Activity Update " + updatedActivity, Toast.LENGTH_SHORT).show();

             if (locationUpdateCallback != null) {
                    // cordova.getThreadPool().execute(new Runnable() {
                    //     public void run() {
                    //         if(intent.getExtras() == null) {
                    //             locationUpdateCallback.error("ERROR: Location Was Null");
                    //         }

                    //         // JSONObject data = locationToJSON(intent.getExtras());
                    //         PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
                    //         pluginResult.setKeepCallback(true);
                    //         locationUpdateCallback.sendPluginResult(pluginResult);
                    //     }
                    // });
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
                    pluginResult.setKeepCallback(true);
                    locationUpdateCallback.sendPluginResult(pluginResult);
                } else {
                    Log.w(TAG, "WARNING LOCATION UPDATE CALLBACK IS NULL, PLEASE RUN REGISTER LOCATION UPDATES");
                }

        }
    }

    protected synchronized void buildActivityClient() {
        GoogleApiClient.ConnectionCallbacks cb = new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                Log.w(TAG, "Activity Client Connected");
                ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                        mGoogleActivityClient,
                        500,
                        getActivityPendingIntent()
                );
                //.setResultCallback(BackgroundLocationServicesPlugin.this);
            }
            @Override
            public void onConnectionSuspended(int i) {
//                Log.w(TAG, "Connection To Activity Suspended");
//                Toast.makeText(getApplicationContext(), "Activity Client Suspended", Toast.LENGTH_SHORT).show();
//                if(isEnabled) {
//                    mGoogleActivityClient.connect();
//                }
            }
        };

        GoogleApiClient.OnConnectionFailedListener failedCb = new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult cr) {
                Log.w(TAG, "ERROR CONNECTING TO DETECTED ACTIVITIES");
            }
        };

        mGoogleActivityClient = new GoogleApiClient.Builder(webView.getContext())
                .addConnectionCallbacks(cb)
                .addOnConnectionFailedListener(failedCb)
                .addApiIfAvailable(ActivityRecognition.API)
                .build();
    }

    private PendingIntent getActivityPendingIntent() {
        Intent intent = new Intent(webView.getContext(), DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(webView.getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    private void createLocationUpdateReceiver() {
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                if(debug()) {
                    Log.d(TAG, "Location Received, ready for callback");
                }
                if (locationUpdateCallback != null) {
                    cordova.getThreadPool().execute(new Runnable() {
                        public void run() {
                            if(intent.getExtras() == null) {
                                locationUpdateCallback.error("ERROR: Location Was Null");
                            }

                            JSONObject data = locationToJSON(intent.getExtras());
                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, data);
                            pluginResult.setKeepCallback(true);
                            locationUpdateCallback.sendPluginResult(pluginResult);
                        }
                    });
                } else {
                    Log.w(TAG, "WARNING LOCATION UPDATE CALLBACK IS NULL, PLEASE RUN REGISTER LOCATION UPDATES");
                }
            }
        };
    }
    
    private void destroyLocationUpdateReceiver() {
        if (this.receiver != null) {
            try {
                webView.getContext().unregisterReceiver(this.receiver);
                this.receiver = null;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error unregistering location receiver: ", e);
            }
        }
    }
    
    private JSONObject locationToJSON(Bundle b) {
        JSONObject data = new JSONObject();
        try {
            data.put("latitude", b.getDouble("latitude"));
            data.put("longitude", b.getDouble("longitude"));
            data.put("accuracy", b.getDouble("accuracy"));
            data.put("altitude", b.getDouble("altitude"));
            data.put("timestamp", b.getDouble("timestamp"));
            data.put("speed", b.getDouble("speed"));
            data.put("heading", b.getDouble("heading"));
        } catch(JSONException e) {
            Log.d(TAG, "ERROR CREATING JSON" + e);
        }
        
        return data;
    }
    
    
    /**
     * Override method in CordovaPlugin.
     * Checks to see if it should turn off
     */
    public void onDestroy() {
        Activity activity = this.cordova.getActivity();
        
        if(isEnabled && stopOnTerminate.equalsIgnoreCase("true")) {
            activity.stopService(updateServiceIntent);
        }
    }
}