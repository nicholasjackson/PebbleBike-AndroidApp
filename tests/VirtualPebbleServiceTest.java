import android.app.Service;
import android.content.Intent;
import android.test.ServiceTestCase;
import com.google.inject.AbstractModule;
import com.njackson.VirtualPebbleService;
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
import roboguice.activity.RoboActivity;
import com.google.inject.util.Modules;
import roboguice.service.RoboService;

@RunWith(RobolectricTestRunner.class)
public class VirtualPebbleServiceTest {

    private VirtualPebbleService _service;

    protected Application application;// = mock(Application.class, RETURNS_DEEP_STUBS);
    protected Context context = mock(VirtualPebbleService.class, RETURNS_DEEP_STUBS);
    protected IMessageManager _mockMessageManager = mock(IMessageManager.class);

    @Before
    public void setUp() {

        _service = new VirtualPebbleService();
        application = _service.getApplication();

        RoboGuice.setBaseApplicationInjector(application, RoboGuice.DEFAULT_STAGE,
                RoboGuice.newDefaultRoboModule(application),
                new MyTestModule());

    }

    @After
    public void tearDown() {
        RoboGuice.util.reset();//reset robo guice to prevent poloution
    }

    @Test
    public void serviceShouldStartTest() {

        _service.onCreate();
        Intent startIntent = new Intent(context, VirtualPebbleService.class);
        int res = _service.onStartCommand(startIntent,0,42);

        assertEquals(Service.START_STICKY, res);
        Mockito.verify(_mockMessageManager,Mockito.times(1)).doStuff();

        _service.onDestroy();
    }

    @Test
    public void serviceShouldRespondToIntent() {

    }

    public class MyTestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(IMessageManager.class).toInstance(_mockMessageManager);
        }
    }

}
