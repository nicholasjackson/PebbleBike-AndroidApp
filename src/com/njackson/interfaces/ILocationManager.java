package com.njackson.interfaces;

import android.location.GpsStatus;
import android.location.LocationListener;

/**
 * Created by server on 20/03/2014.
 */
public interface ILocationManager {

    public void removeUpdates(LocationListener listener);
    public void removeNmeaListener(GpsStatus.NmeaListener listener);
    public void requestLocationUpdates(String gpsProvider, int refresh_interval, int i, LocationListener locationListener);
    public void addNmeaListener(GpsStatus.NmeaListener mNmeaListener);
    boolean isProviderEnabled(String gpsProvider);

}
