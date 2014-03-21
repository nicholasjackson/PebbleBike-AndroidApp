package com.njackson.tests.service;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.njackson.events.GPSService.GPSDisabledEvent;
import com.njackson.events.GPSService.GPSRefreshChangeEvent;
import com.njackson.events.GPSService.GPSResetEvent;
import com.njackson.events.GPSService.NewLocationEvent;
import com.njackson.gps.GPSService;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import roboguice.RoboGuice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created by server on 20/03/2014.
 */
@RunWith(RobolectricTestRunner.class)
public class GPSServiceTest {

    private GPSService _service;

    protected Context context = mock(Application.class, RETURNS_DEEP_STUBS);
    protected LocationManager _mockLocationManager;
    private SharedPreferences _mockPreferences;
    private SharedPreferences.Editor _mockEditor;

    private NewLocationEvent _locationEventResults;
    private Bus _bus = new Bus();
    private GPSDisabledEvent _gpsDisabledEvent;

    public class DITestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(LocationManager.class).toInstance(_mockLocationManager);
            bind(Bus.class).toInstance(_bus);
            bind(SharedPreferences.class).toInstance(_mockPreferences);
            //bind(Bus.class).in(Singleton.class);
        }
    }

    private void setupGuice(Application application) {
        Module overiddenModule = Modules.override(RoboGuice.newDefaultRoboModule(application)).with(new DITestModule());
        RoboGuice.setBaseApplicationInjector(application, RoboGuice.DEFAULT_STAGE,
                overiddenModule);
    }

    private void setupMocks() {
        _mockLocationManager = mock(LocationManager.class, RETURNS_DEEP_STUBS);
        _mockPreferences = mock(SharedPreferences.class,RETURNS_DEEP_STUBS);
        _mockEditor = mock(SharedPreferences.Editor.class,RETURNS_DEEP_STUBS);

        when(_mockPreferences.edit()).thenReturn(_mockEditor);
    }

    //Bus Subscriptions
    @Subscribe
    public void onNewLocationEvent(NewLocationEvent event) {
        _locationEventResults = event;
    }
    @Subscribe
    public void onGPSDisabledEvent(GPSDisabledEvent event) {
        _gpsDisabledEvent = event;
    }

    @Before
    public void setUp() {
        setupMocks();

        _locationEventResults = null; // reset the event results
        _bus.register(this); // register for bus notifications

        _service = new GPSService();
        setupGuice(_service.getApplication());
    }

    private void StartService() {
        _service.onCreate();
        Intent startIntent = new Intent(context, GPSService.class);
        int res = _service.onStartCommand(startIntent,0,42);
        assertEquals(Service.START_STICKY, res);
    }

    @After
    public void tearDown() {
        _service.onDestroy();
        reset(_mockEditor);
        reset(_mockLocationManager);
        reset(_mockPreferences);
    }

    @Test
    public void testSetsAdvancedLocationDebugLevel() {
        assertNotNull(null);
    }

    @Test
    public void testBroadcastEventOnLocationDisabled() throws InterruptedException {
        when(_mockLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(false);

        StartService();

        Thread.sleep(100);
        assertNotNull(_gpsDisabledEvent);
    }

    @Test
    public void testBroadcastEventOnLocationChange() throws InterruptedException {
        when(_mockLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true);

        StartService();

        ArgumentCaptor<LocationListener> locationListenerCaptor = ArgumentCaptor.forClass(LocationListener.class);
        verify(_mockLocationManager).requestLocationUpdates(
                anyString(),
                anyLong(),
                anyFloat(),
                locationListenerCaptor.capture());

        Location location = new Location("location");
        LocationListener listenerArgument = locationListenerCaptor.getValue();
        listenerArgument.onLocationChanged(location);

        Thread.sleep(100);
        assertNotNull(_locationEventResults);
    }

    @Test
    public void testHandlesGPSLocationReset() throws InterruptedException {
        StartService();

        _bus.post(new GPSResetEvent());

        Thread.sleep(100);
        verify(_mockEditor,times(1)).putFloat("GPS_SPEED",0.0f);
        verify(_mockEditor, times(1)).commit();
    }

    @Test
    public void testHandlesRefreshInterval() throws InterruptedException {
        when(_mockLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)).thenReturn(true);

        StartService();

        ArgumentCaptor<LocationListener> locationListenerCaptor = ArgumentCaptor.forClass(LocationListener.class);
        verify(_mockLocationManager).requestLocationUpdates(
                anyString(),
                anyLong(),
                anyFloat(),
                locationListenerCaptor.capture());

        int refreshInterval = 200;
        _bus.post(new GPSRefreshChangeEvent(refreshInterval));

        Thread.sleep(100);
        verify(_mockLocationManager, times(1)).removeUpdates((LocationListener)anyObject());
        verify(_mockLocationManager, times(1)).requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                refreshInterval,
                2,
                locationListenerCaptor.getValue()
        );
    }

}
