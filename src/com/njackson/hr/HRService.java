package com.njackson.hr;

import android.content.Intent;
import android.os.IBinder;
import roboguice.service.RoboService;

/**
 * Created by server on 20/03/2014.
 */
public class HRService extends RoboService {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //handleIntent(intent);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
