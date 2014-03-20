package com.njackson.tests.service;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import com.njackson.hr.HRService;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import roboguice.RoboGuice;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * Created by server on 20/03/2014.
 */
@RunWith(RobolectricTestRunner.class)
public class HRServiceTest {

    HRService _service;
    protected Context context = mock(Application.class, RETURNS_DEEP_STUBS);

    @Before
    public void setUp() {
        _service = new HRService();
        _service.onCreate();
        Intent startIntent = new Intent(context, HRService.class);
        int res = _service.onStartCommand(startIntent,0,42);

        assertEquals(Service.START_STICKY, res);
    }

    @After
    public void tearDown() {
        _service.onDestroy();
        RoboGuice.util.reset();//reset robo guice to prevent poloution
    }



}
