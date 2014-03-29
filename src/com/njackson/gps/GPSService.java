package com.njackson.gps;

import android.app.Notification;
import android.app.PendingIntent;
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
import android.util.Log;

import com.njackson.*;
import com.njackson.events.GPSService.GPSDisabledEvent;
import com.njackson.events.GPSService.GPSRefreshChangeEvent;
import com.njackson.events.GPSService.GPSResetEvent;
import com.njackson.events.GPSService.NewLocationEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
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

    public static final String OUT_NEW_GPS_LOCATION = "com.njackson.gps.GPSService.NewLocation";
    public static final String OUT_GPS_DISABLED = "com.njackson.gps.GPSService.NewLocation";
    public static final String IN_SET_GPS_REFRESH = "com.njackson.gps.GPSService.NewLocation";

    @Inject private LocationManager _locationMgr;
    @Inject private SensorManager _mSensorMgr;
    @Inject private SharedPreferences _sharedPreferences;
    @Inject Bus _bus;

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
        _bus.register(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private boolean checkGPSEnabled(LocationManager locationMgr) {
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

    //Bus Subscriptions
    @Subscribe
    public void onGPSResetEvent(GPSResetEvent event) {
        resetGPSStats();
    }

    @Subscribe
    public void onGPSRefreshChangeEvent(GPSRefreshChangeEvent event) {
        changeRefreshInterval(event.getRefreshInterval());
    }

    // load the saved state
    private void loadGPSStats() {
    	Log.d(TAG, "loadGPSStats()");

        _speed = _sharedPreferences.getFloat("GPS_SPEED",0.0f);
        _distance = _sharedPreferences.getFloat("GPS_DISTANCE",0.0f);
        _advancedLocation.setDistance(_distance);
        _advancedLocation.setElapsedTime(_sharedPreferences.getLong("GPS_ELAPSEDTIME", 0));
        
        try {
            _advancedLocation.setAscent(_sharedPreferences.getFloat("GPS_ASCENT", 0.0f));
        } catch (ClassCastException e) {
            _advancedLocation.setAscent(0.0);
        }
        try {
            _updates = _sharedPreferences.getInt("GPS_UPDATES",0);
        } catch (ClassCastException e) {
            _updates = 0;
        }
        
        if (_sharedPreferences.contains("GPS_FIRST_LOCATION_LAT") && _sharedPreferences.contains("GPS_FIRST_LOCATION_LON")) {
            firstLocation = new Location("PebbleBike");
            firstLocation.setLatitude(_sharedPreferences.getFloat("GPS_FIRST_LOCATION_LAT", 0.0f));
            firstLocation.setLongitude(_sharedPreferences.getFloat("GPS_FIRST_LOCATION_LON", 0.0f));
        } else {
            firstLocation = null;
        }
    }

    // save the state
    private void saveGPSStats() {

        SharedPreferences.Editor editor = _sharedPreferences.edit();
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
    private void resetGPSStats() {
	    SharedPreferences.Editor editor = _sharedPreferences.edit();
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
        _advancedLocation.debugTagPrefix = "PB-";
        //_advancedLocation.debugLevel = MainActivity.debug ? 2 : 0;

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

        // check to see if GPS is enabled
        if(checkGPSEnabled(_locationMgr)) {
            requestLocationUpdates(intent.getIntExtra("REFRESH_INTERVAL", 1000));

            SharedPreferences.Editor editor = _sharedPreferences.edit();
            editor.putLong("GPS_LAST_START", System.currentTimeMillis());
            editor.commit();

        }else {
            _bus.post(new GPSDisabledEvent());
            return;
        }

        // delay between events in microseconds
        _mSensorMgr.registerListener(mSensorListener, _mSensorMgr.getDefaultSensor(Sensor.TYPE_PRESSURE), 3000000);
    }

    private void requestLocationUpdates(int refresh_interval) {

        _refresh_interval = refresh_interval;

        if (_gpsStarted) {
            _locationMgr.removeUpdates(_locationListener);
        }
        _locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, (long)_refresh_interval, 2.0f, _locationListener);

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
                   _advancedLocation.setGeoidHeight(geoid_height);
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
                _advancedLocation.onAltitudeChanged(altitude);

                broadcastLocation();
            }
        }
    };
    
    private void broadcastLocation() {
        NewLocationEvent event = new NewLocationEvent();

        event.setSpeed(_advancedLocation.getSpeed());
        event.setDistance(_advancedLocation.getDistance());
        event.setAvgSpeed(_advancedLocation.getAverageSpeed());
        event.setLatitude(_advancedLocation.getLatitude());
        event.setLongitude(_advancedLocation.getLongitude());
        event.setAltitude(_advancedLocation.getAltitude()); // m
        event.setAscent(_advancedLocation.getAscent()); // m
        event.setAscentRate(3600f * _advancedLocation.getAscentRate()); // in m/h
        event.setSlope(100f * _advancedLocation.getSlope()); // in %
        event.setAccuracy(_advancedLocation.getAccuracy()); // m
        event.setTime(_advancedLocation.getElapsedTime());
        event.setXpos(xpos);
        event.setYpos(ypos);
        event.setBearing(_advancedLocation.getBearing());

        _bus.post(event);
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
