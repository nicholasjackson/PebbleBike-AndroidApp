package com.njackson.util;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by server on 29/03/2014.
 */
public class Services {

    public boolean CheckServiceRunning(Context context, String serviceName) {

        ActivityManager manager = (ActivityManager) context.getSystemService(serviceName);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;

    }

}
