package com.njackson;

import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.squareup.otto.Bus;
import de.cketti.library.changelog.ChangeLog;
import roboguice.activity.RoboActivity;

import javax.inject.Inject;

public class MainActivity extends RoboActivity {
	
	private static final String TAG = "PB-MainActivity";

    @Inject private SharedPreferences _sharedPreferences;
    @Inject Bus _bus;

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
