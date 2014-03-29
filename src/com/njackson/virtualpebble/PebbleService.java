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
import com.njackson.events.GPSService.NewLocationEvent;
import com.njackson.interfaces.IMessageManager;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
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
    @Inject Bus _bus;

    private final String TAG = "PB-VirtualPebble";

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
        _bus.register(this);
    }

    @Override
    public void onDestroy (){
    }

    private void handleIntent(Intent intent) {
        _messageManager.setContext(getApplicationContext());
        new Thread(_messageManager).start();
    }

    @Subscribe
    public void onNewLocationEvent(NewLocationEvent newLocation) {
        PebbleDictionary dictionary = LocationEventConverter.Convert(newLocation,false,false,false,1000,0);
        sendDataToPebble(dictionary);
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