package com.njackson.virtualpebble;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.inject.Injector;
import com.njackson.Constants;
import com.njackson.interfaces.IMessageManager;
import org.json.JSONException;
import roboguice.RoboGuice;
import roboguice.inject.ContextScopedRoboInjector;
import roboguice.service.RoboService;

import javax.inject.Inject;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static roboguice.RoboGuice.getBaseApplicationInjector;

public class PebbleService extends RoboService {

    @Inject IMessageManager _messageManager;

    private final String TAG = "PB-VirtualPebble";
    public static final String PEBBLE_DATA_EVENT = "PEBBLE_DATA_EVENT";
    public static final String INTENT_EXTRA_NAME = "PEBBLE_DATA";

    public static Application app;

    private BroadcastReceiver _broadcastReceiver;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void registerBroadcastReceiver() {
        _broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String jsonString = intent.getStringExtra(INTENT_EXTRA_NAME);
                Log.w(TAG,"Got Data:" + jsonString);
                try {
                    PebbleDictionary data = PebbleDictionary.fromJson(jsonString);
                    sendDataToPebble(data);
                }catch (JSONException e) {
                    Log.w(TAG,"Error decoding json data");
                }
            }
        };
        IntentFilter dataFilter = new IntentFilter(PEBBLE_DATA_EVENT);
        registerReceiver(_broadcastReceiver,dataFilter);
    }

    @Override
    public void onDestroy (){
        unregisterReceiver(_broadcastReceiver);
    }

    private void handleIntent(Intent intent) {
        registerBroadcastReceiver();
        _messageManager.setContext(getApplicationContext());
        new Thread(_messageManager).start();
    }

    private void sendDataToPebble(PebbleDictionary data) {
        _messageManager.offer(data);
    }
    private void sendDataToPebbleIfPossible(PebbleDictionary data) {
        _messageManager.offerIfLow(data, 5);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}