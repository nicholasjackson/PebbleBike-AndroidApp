package com.njackson.tests.service;

import android.app.Service;
import android.content.Intent;
import com.google.inject.AbstractModule;
import com.njackson.virtualpebble.PebbleService;
import com.njackson.interfaces.IMessageManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by server on 14/03/2014.
 */

import android.app.Application;
import android.content.Context;
import roboguice.RoboGuice;

@RunWith(RobolectricTestRunner.class)
public class PebbleServiceTest {

    private PebbleService _service;

    protected Application application;// = mock(Application.class, RETURNS_DEEP_STUBS);
    protected Context context = mock(Application.class, RETURNS_DEEP_STUBS);
    protected IMessageManager _mockMessageManager = mock(IMessageManager.class);

    public class DITestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(IMessageManager.class).toInstance(_mockMessageManager);
        }
    }

    @Before
    public void setUp() {
        _service = new PebbleService();
        application = _service.getApplication();

        RoboGuice.setBaseApplicationInjector(application, RoboGuice.DEFAULT_STAGE,
                RoboGuice.newDefaultRoboModule(application),
                new DITestModule());

        _service.onCreate();
        Intent startIntent = new Intent(context, PebbleService.class);
        int res = _service.onStartCommand(startIntent,0,42);

        assertEquals(Service.START_STICKY, res);
    }

    @After
    public void tearDown() {
        _service.onDestroy();
        RoboGuice.util.reset();//reset robo guice to prevent poloution
    }

    @Test
    public void serviceSetsApplicationContext() {
        Mockito.verify(_mockMessageManager,Mockito.times(1)).setContext(application);
    }
}
