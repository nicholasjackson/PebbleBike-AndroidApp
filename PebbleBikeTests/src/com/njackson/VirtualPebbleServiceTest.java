package com.njackson;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.test.ServiceTestCase;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.njackson.virtualpebble.PebbleService;

/**
 * Created by server on 11/03/2014.
*/
public class VirtualPebbleServiceTest extends ServiceTestCase<PebbleService> {

    private BroadcastReceiver receiver = null;
    private Bundle results = null;

    public VirtualPebbleServiceTest() {
        super(PebbleService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        IntentFilter filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        getSystemContext().registerReceiver(receiver, filter);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (receiver != null)
            getSystemContext().unregisterReceiver(receiver);
    }

    public void testBattery100Percent() throws InterruptedException {
        PebbleDictionary dic = new PebbleDictionary();
        dic.addInt32(Constants.MSG_BATTERY_LEVEL, 100);

        Intent startIntent = new Intent();
        startIntent.putExtra("PEBBLE_DATA", dic.toJsonString());
        startIntent.setClass(getContext(), PebbleService.class);

        getSystemContext().startService(startIntent);
        Thread.sleep(100);

        //assertNotNull(results);
    }
}