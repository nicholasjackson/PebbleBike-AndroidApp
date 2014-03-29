package com.njackson;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.PebbleKit.FirmwareVersionInfo;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;

import com.njackson.activityrecognition.ActivityRecognitionIntentService;
import com.njackson.gps.GPSService;
import com.njackson.util.AltitudeGraphReduce;
import com.njackson.virtualpebble.PebbleService;
import de.cketti.library.changelog.ChangeLog;
import roboguice.activity.RoboActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends RoboActivity {
	
	private static final String TAG = "PB-MainActivity";

    public void loadPreferences() {
    	loadPreferences(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
    }

    public void loadPreferences(SharedPreferences prefs) {
        //setup the defaults

        debug = prefs.getBoolean("PREF_DEBUG", false);

        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME,0);

        hrm_name = settings.getString("hrm_name", "");
        hrm_address = settings.getString("hrm_address", "");
        if (debug) Log.d(TAG, "hrm_name:" + hrm_name + " " + hrm_address);

        _activityRecognition = prefs.getBoolean("ACTIVITY_RECOGNITION",false);
        _liveTracking = prefs.getBoolean("LIVE_TRACKING",false);
        oruxmaps_autostart = prefs.getString("ORUXMAPS_AUTO", "disable");

        if(_activityRecognition)
            initActivityRecognitionClient();
        else
            stopActivityRecogntionClient();

        HomeActivity activity = getHomeScreen();
        if(activity != null)
            activity.setStartButtonVisibility(!_activityRecognition);

        try {
        	//setConversionUnits(Integer.valueOf(prefs.getString("UNITS_OF_MEASURE", "0")));
        } catch (Exception e) {
        	Log.e(TAG, "Exception:" + e);
        }
        try {
            int prev_refresh_interval = _refresh_interval;
            _refresh_interval = Integer.valueOf(prefs.getString("REFRESH_INTERVAL", "1000"));
            if (prev_refresh_interval != _refresh_interval) {
                //GPSService.changeRefreshInterval(_refresh_interval);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception converting REFRESH_INTERVAL:" + e);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        ChangeLog cl = new ChangeLog(this);
        if (cl.isFirstRun()) {
          cl.getLogDialog().show();
        }
        
        //checkGooglePlayServices();

        //loadPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
