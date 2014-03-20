package com.njackson.gps;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.njackson.*;
import com.njackson.interfaces.ILocationManager;
import fr.jayps.android.AdvancedLocation;
import roboguice.service.RoboService;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: njackson
 * Date: 23/05/2013
 * Time: 13:30
 * To change this template use File | Settings | File Templates.
 */
public class GPSService extends RoboService {
	
	private static final String TAG = "PB-GPSService";

    @Inject private ILocationManager _locationMgr;
    @Inject private SensorManager _mSensorMgr;

    private int _updates;
    private float _speed;
    private float _averageSpeed;
    private float _distance;

    private float _prevspeed = -1;
    private float _prevaverageSpeed = -1;
    private float _prevdistance = -1;
    private double _prevaltitude = -1;
    private long _prevtime = -1;
    private long _lastSaveGPSTime = 0;

    double xpos = 0;
    double ypos = 0;
    Location firstLocation = null;
    private AdvancedLocation _advancedLocation;

    private int _refresh_interval = 1000;
    private boolean _gpsStarted = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleCommand(intent);
        makeServiceForeground("Pebble Bike", "GPS started");
        return START_STICKY;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private boolean checkGPSEnabled(ILocationManager locationMgr) {
        if(!locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
           return false;
        } else {
            return true;
        }
    }

    @Override
    public void onDestroy (){
        Log.d(TAG, "Stopped GPS Service");
        saveGPSStats();
        removeServiceForeground();

        _locationMgr.removeUpdates(_locationListener);
        _locationMgr.removeNmeaListener(mNmeaListener);
        _mSensorMgr.unregisterListener(mSensorListener);
    }

    // load the saved state
    public void loadGPSStats() {
    	Log.d(TAG, "loadGPSStats()");
    	
    	SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME,0);
        _speed = settings.getFloat("GPS_SPEED",0.0f);
        _distance = settings.getFloat("GPS_DISTANCE",0.0f);
        _advancedLocation.setDistance(_distance);
        _advancedLocation.setElapsedTime(settings.getLong("GPS_ELAPSEDTIME", 0));
        
        try {
            _advancedLocation.setAscent(settings.getFloat("GPS_ASCENT", 0.0f));
        } catch (ClassCastException e) {
            _advancedLocation.setAscent(0.0);
        }
        try {
            _updates = settings.getInt("GPS_UPDATES",0);
        } catch (ClassCastException e) {
            _updates = 0;
        }
        
        if (settings.contains("GPS_FIRST_LOCATION_LAT") && settings.contains("GPS_FIRST_LOCATION_LON")) {
            firstLocation = new Location("PebbleBike");
            firstLocation.setLatitude(settings.getFloat("GPS_FIRST_LOCATION_LAT", 0.0f));
            firstLocation.setLongitude(settings.getFloat("GPS_FIRST_LOCATION_LON", 0.0f));
        } else {
            firstLocation = null;
        }
        
    }

    // save the state
    public void saveGPSStats() {

        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("GPS_SPEED", _speed);
        editor.putFloat("GPS_DISTANCE",_distance);
        editor.putLong("GPS_ELAPSEDTIME", _advancedLocation.getElapsedTime());
        editor.putFloat("GPS_ASCENT", (float) _advancedLocation.getAscent());
        editor.putInt("GPS_UPDATES", _updates);
        if (firstLocation != null) {
            editor.putFloat("GPS_FIRST_LOCATION_LAT", (float) firstLocation.getLatitude());
            editor.putFloat("GPS_FIRST_LOCATION_LON", (float) firstLocation.getLongitude());
        }
        editor.commit();
    }

    // reset the saved state
    public void resetGPSStats(SharedPreferences settings) {
    	Log.d(TAG, "resetGPSStats()");
    	
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putFloat("GPS_SPEED", 0.0f);
	    editor.putFloat("GPS_DISTANCE",0.0f);
	    editor.putLong("GPS_ELAPSEDTIME", 0);
	    editor.putFloat("GPS_ASCENT", 0.0f);
	    editor.putInt("GPS_UPDATES", 0);
        editor.remove("GPS_FIRST_LOCATION_LAT");
        editor.remove("GPS_FIRST_LOCATION_LON");
	    editor.commit();

        // GPS is running
        // reninit all properties
        _advancedLocation = new AdvancedLocation(getApplicationContext());
        _advancedLocation.debugLevel = MainActivity.debug ? 2 : 0;
        _advancedLocation.debugTagPrefix = "PB-";

        loadGPSStats();
    }

    private void changeRefreshInterval(int refresh_interval) {
        _refresh_interval = refresh_interval;
        requestLocationUpdates(refresh_interval);
    }

    private void handleCommand(Intent intent) {
        Log.d(TAG, "Started GPS Service");

        _advancedLocation = new AdvancedLocation(getApplicationContext());
        _advancedLocation.debugTagPrefix = "PB-";

        loadGPSStats();
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // check to see if GPS is enabled
        if(checkGPSEnabled(_locationMgr)) {
            requestLocationUpdates(intent.getIntExtra("REFRESH_INTERVAL", 1000));

            SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME,0);

            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("GPS_LAST_START", System.currentTimeMillis());
            editor.commit();
            
            // send the saved values directly to update pebble
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.GPSServiceReceiver.ACTION_RESP);
            broadcastIntent.putExtra("DISTANCE", _advancedLocation.getDistance());
            broadcastIntent.putExtra("AVGSPEED", _advancedLocation.getAverageSpeed());
            broadcastIntent.putExtra("ASCENT", _advancedLocation.getAscent());
            sendBroadcast(broadcastIntent);
        }else {
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.GPSServiceReceiver.ACTION_GPS_DISABLED);
            sendBroadcast(broadcastIntent);
            return;
        }

        // delay between events in microseconds
        _mSensorMgr.registerListener(mSensorListener, _mSensorMgr.getDefaultSensor(Sensor.TYPE_PRESSURE), 3000000);
    }

    private void requestLocationUpdates(int refresh_interval) {
        Log.d(TAG, "_requestLocationUpdates("+refresh_interval+")");
        _refresh_interval = refresh_interval;

        if (_gpsStarted) {
            _locationMgr.removeUpdates(_locationListener);
        }
        _locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, _refresh_interval, 2, _locationListener);

        _gpsStarted = true;
    }

    private LocationListener _locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            _advancedLocation.onLocationChanged(location);

            _speed = _advancedLocation.getSpeed();
            if(_speed < 1) {
                _speed = 0;
            } else {
                _updates++;
            }

            _averageSpeed = _advancedLocation.getAverageSpeed();
            _distance = _advancedLocation.getDistance();

            if (firstLocation == null) {
                firstLocation = location;
            }

            boolean send = false;
            if (_speed != _prevspeed || _averageSpeed != _prevaverageSpeed || _distance != _prevdistance || _prevaltitude != _advancedLocation.getAltitude()) {
                send = true;

                _prevaverageSpeed = _averageSpeed;
                _prevdistance = _distance;
                _prevspeed = _speed;
                _prevaltitude = _advancedLocation.getAltitude();
                _prevtime = _advancedLocation.getTime();
            } else if (_prevtime + 5000 < _advancedLocation.getTime()) {
                send = true;
                _prevtime = _advancedLocation.getTime();
            }
            if (send) {
                broadcastLocation();

                if (_lastSaveGPSTime == 0 || (_advancedLocation.getTime() - _lastSaveGPSTime > 60000)) {
                    saveGPSStats();
                    _lastSaveGPSTime = _advancedLocation.getTime();
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

    };

    NmeaListener mNmeaListener = new NmeaListener() {
        @Override
        public void onNmeaReceived(long timestamp, String nmea) {
           if (nmea.startsWith("$GPGGA")) {

               String[] strValues = nmea.split(",");

               try {
                   // Height of geoid above WGS84 ellipsoid
                   double geoid_height = Double.parseDouble(strValues[11]);

                   if (MainActivity.debug) Log.d(TAG, "nmea geoid_height: " + geoid_height);
                   _advancedLocation.setGeoidHeight(geoid_height);
                   MainActivity.geoidHeight = geoid_height;

                   _locationMgr.removeNmeaListener(mNmeaListener);
               } catch (Exception e) {
               }
           }
        }
    };

    private SensorEventListener mSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            float pressure_value = 0.0f;
            double altitude = 0.0f;

            // we register to TYPE_PRESSURE, so we don't really need this test
            if( Sensor.TYPE_PRESSURE == event.sensor.getType()) {
                pressure_value = event.values[0];
                altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure_value);
                if (MainActivity.debug) Log.d(TAG, "pressure_value=" + pressure_value + " altitude=" + altitude);

                _advancedLocation.onAltitudeChanged(altitude);

                broadcastLocation();
            }
        }
    };
    
    private void broadcastLocation() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.GPSServiceReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("SPEED",       _advancedLocation.getSpeed());
        broadcastIntent.putExtra("DISTANCE",    _advancedLocation.getDistance());
        broadcastIntent.putExtra("AVGSPEED",    _advancedLocation.getAverageSpeed());
        broadcastIntent.putExtra("LAT",         _advancedLocation.getLatitude());
        broadcastIntent.putExtra("LON",         _advancedLocation.getLongitude());
        broadcastIntent.putExtra("ALTITUDE",    _advancedLocation.getAltitude()); // m
        broadcastIntent.putExtra("ASCENT",      _advancedLocation.getAscent()); // m
        broadcastIntent.putExtra("ASCENTRATE",  (3600f * _advancedLocation.getAscentRate())); // in m/h
        broadcastIntent.putExtra("SLOPE",       (100f * _advancedLocation.getSlope())); // in %
        broadcastIntent.putExtra("ACCURACY",   _advancedLocation.getAccuracy()); // m
        broadcastIntent.putExtra("TIME",        _advancedLocation.getElapsedTime());
        broadcastIntent.putExtra("XPOS",        xpos);
        broadcastIntent.putExtra("YPOS",        ypos);
        broadcastIntent.putExtra("BEARING",     _advancedLocation.getBearing());
        sendBroadcast(broadcastIntent);
    }

    private void makeServiceForeground(String titre, String texte) {
        final int myID = 1000;

        //The intent to launch when the user clicks the expanded notification
        Intent i = new Intent(this, MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, i, 0);

        // The following code is deprecated since API 11 (Android 3.x). Notification.Builder could be used instead, but without Android 2.x compatibility 
        Notification notification = new Notification(R.drawable.ic_launcher, "Pebble Bike", System.currentTimeMillis());
        notification.setLatestEventInfo(this, titre, texte, pendIntent);

        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(myID, notification);
    }

    private void removeServiceForeground() {
        stopForeground(true);
    }

}
