package com.njackson.tests.service;

import android.app.Application;
import android.content.*;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.njackson.activityrecognition.ActivityRecognitionIntentService;
import com.njackson.events.ActivityRecognitionService.NewActivityEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import roboguice.RoboGuice;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by server on 21/03/2014.
 */
@RunWith(RobolectricTestRunner.class)
public class ActivityRecognitionServiceTest {

    private Bus _bus = new Bus();
    private MyBroadcastIntentServiceMock _service;
    private NewActivityEvent _event;

    public class DITestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(Bus.class).toInstance(_bus);
        }
    }

    private void setupGuice(Application application) {
        Module overiddenModule = Modules.override(RoboGuice.newDefaultRoboModule(application)).with(new DITestModule());
        RoboGuice.setBaseApplicationInjector(application, RoboGuice.DEFAULT_STAGE,
                overiddenModule);
    }

    @Before
    public void setUp(){
        _bus.register(this);
        _service = new MyBroadcastIntentServiceMock("ACIntentService");
        setupGuice(_service.getApplication());
    }

    @After
    public void tearDown() {
        _service.onDestroy();
        _event = null;
    }

    @Subscribe
    public void onNewActivityEvent(NewActivityEvent event) {
        _event = event;
    }

    @Test
    public void testRecieveBicycle() throws InterruptedException {
        startWithType(DetectedActivity.ON_BICYCLE);
        assertNotNull(_event);
        assertEquals(_event.getActivityType(), DetectedActivity.ON_BICYCLE);
    }

    @Test
    public void testRecieveOnFoot() throws InterruptedException {
        startWithType(DetectedActivity.ON_FOOT);
        assertNotNull(_event);
        assertEquals(_event.getActivityType(), DetectedActivity.ON_FOOT);
    }

    private void startWithType(int activityType) throws InterruptedException {
        DetectedActivity activity = new DetectedActivity(activityType, 1);
        ActivityRecognitionResult result = new ActivityRecognitionResult(activity, 1000, 1000);

        Context context = Robolectric.getShadowApplication().getApplicationContext();

        Intent startIntent = new Intent();
        startIntent.putExtra(ActivityRecognitionResult.EXTRA_ACTIVITY_RESULT, result);

        _service.onCreate();
        _service.onHandleIntent(startIntent);

        Thread.sleep(100);
    }

    class MyBroadcastIntentServiceMock extends ActivityRecognitionIntentService {
        public MyBroadcastIntentServiceMock(String name) {
            super(name);
        }

        @Override
        public void onHandleIntent(Intent intent) {
            super.onHandleIntent(intent);
        }
    }

}
