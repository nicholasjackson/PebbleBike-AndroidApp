package com.njackson.tests.converters;

import com.getpebble.android.kit.util.PebbleDictionary;
import com.njackson.Constants;
import com.njackson.events.GPSService.NewLocationEvent;
import com.njackson.virtualpebble.LocationEventConverter;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by server on 25/03/2014.
 */
public class LocationEventConverterTest {

    @Test
    public void testConvertsNewLocationEventToDictionary() {

        NewLocationEvent event = new NewLocationEvent();
        event.setUnits(1);
        event.setBearing(1.1);
        event.setYpos(2.2);
        event.setXpos(3.3);
        event.setTime(4);
        event.setAvgSpeed(5.5f);
        event.setAccuracy(6.6f);
        event.setAltitude(7.7);
        event.setAscent(8.8);
        event.setAscentRate(9.9f);
        event.setDistance(10.1f);
        event.setLatitude(11.1);
        event.setLongitude(12.2);
        event.setSlope(13.3f);
        event.setSpeed(14.4f);

        PebbleDictionary dic = LocationEventConverter.Convert(event);
        byte[] data = dic.getBytes(Constants.PEBBLE_LOCTATION_DATA);

        assertNotNull("Data should not be null",data);
        assertEquals("Expected units to be 1",0,getBit(data[0],1));
    }

    private int getBit(byte b, int position)
    {
        return (b >> position) & 1;
    }

}
