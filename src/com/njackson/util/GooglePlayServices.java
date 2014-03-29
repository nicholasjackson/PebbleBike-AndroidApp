package com.njackson.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by server on 29/03/2014.
 */
public class GooglePlayServices {

    public static void Check(Activity activity,Context context) {
        int googlePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if(googlePlayServicesAvailable !=  ConnectionResult.SUCCESS) {
            // google play services need to be updated
            try {
                Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(googlePlayServicesAvailable, activity, 123);
                if(errorDialog != null)
                    errorDialog.show();
            } catch (NoClassDefFoundError e) {
                Toast.makeText(context, "This device is not supported by Google Play Service.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
            }
        }
    }
}
