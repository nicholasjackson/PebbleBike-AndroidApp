package com.njackson.virtualpebble;

import com.getpebble.android.kit.util.PebbleDictionary;
import com.njackson.Constants;
import com.njackson.events.GPSService.NewLocationEvent;

/**
 * Created by server on 25/03/2014.
 * Converts NewLocationEvent to a Pebble Dictionary object
 */
public class LocationEventConverter {

    private static final int POS_UNITS=0;
    private static final int POS_SERVICE_RUNNING=1;

    public static PebbleDictionary Convert(NewLocationEvent event) {

        PebbleDictionary dic = new PebbleDictionary();
        byte[] data = new byte[21];

        data[0] = (byte) ((event.getUnits() % 2) * (1<<POS_UNITS)); // set the units
        data[0] += (byte) ((event.getServiceRunning() ? 1: 0) * (1<<POS_SERVICE_RUNNING));

        dic.addBytes(Constants.PEBBLE_LOCTATION_DATA,data);
        return dic;

    }
}
