package com.njackson.tests.service;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import com.google.inject.AbstractModule;
import com.njackson.MainActivity;
import com.njackson.gps.GPSService;
import com.njackson.interfaces.ILocationManager;
import entities.LocationManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import roboguice.RoboGuice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * Created by server on 20/03/2014.
 */
@RunWith(RobolectricTestRunner.class)
public class GPSServiceTest {

    private GPSService _service;

    protected Application application;// = mock(Application.class, RETURNS_DEEP_STUBS);
    protected Context context = mock(Application.class, RETURNS_DEEP_STUBS);
    protected LocationManager _testLocationManager = new LocationManager();
    private BroadcastReceiver _receiver;
    private Bundle _results;

    public class DITestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ILocationManager.class).toInstance(_testLocationManager);
        }
    }

    @Before
    public void setUp() {
        _service = new GPSService();
        application = _service.getApplication();

        _receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                _results = intent.getExtras();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(MainActivity.GPSServiceReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);

        application.registerReceiver(_receiver, filter);

        RoboGuice.setBaseApplicationInjector(application, RoboGuice.DEFAULT_STAGE,
                RoboGuice.newDefaultRoboModule(application),
                new DITestModule());

        _service.onCreate();

        Intent startIntent = new Intent(context, GPSService.class);
        int res = _service.onStartCommand(startIntent,0,42);

        assertEquals(Service.START_STICKY, res);
    }

    @After
    public void tearDown() {
        _service.onDestroy();
    }

    @Test
    public void testSetsAdvancedLocationDebugLevel() {
        assertNotNull(null);
    }

    @Test
    public void testBroadcastIntentOnLocationChange() throws InterruptedException {
        Location location = new Location("location");
        _testLocationManager.getListener().onLocationChanged(location);

        Thread.sleep(100);

        assertNotNull(_results);
    }

    @Test
    public void testHandlesGPSLocationReset() {
        assertNotNull(null);
    }

    @Test
    public void testHandRefreshInterval() {
        assertNotNull(null);
    }

}
