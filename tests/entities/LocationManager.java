package entities;

import android.location.GpsStatus;
import android.location.LocationListener;
import com.njackson.interfaces.ILocationManager;

/**
 * Created by server on 20/03/2014.
 */
public class LocationManager implements ILocationManager {

    private LocationListener _listener;
    public LocationListener getListener() { return _listener; }

    private Boolean _enabled = true;
    public void setEnabled(Boolean enabled) {
        _enabled = true;
    }

    @Override
    public void removeUpdates(LocationListener listener) {

    }

    @Override
    public void removeNmeaListener(GpsStatus.NmeaListener listener) {

    }

    @Override
    public void requestLocationUpdates(String gpsProvider, int refresh_interval, int i, LocationListener locationListener) {
        _listener = locationListener;
    }

    @Override
    public void addNmeaListener(GpsStatus.NmeaListener mNmeaListener) {

    }

    @Override
    public boolean isProviderEnabled(String gpsProvider) {
        return _enabled;
    }
}
