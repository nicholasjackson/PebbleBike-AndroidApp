package com.njackson;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.njackson.gps.OruxMaps;

public class PebbleDataReceiver extends com.getpebble.android.kit.PebbleKit.PebbleDataReceiver {
	
	private static final String TAG = "PB-PebbleDataReceiver";
	
    public PebbleDataReceiver() {
        super(Constants.WATCH_UUID);
    }
    
    @Override
    public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
    	int  button = -1;
    	int  version = -1;
    	//int live_max_name = -99;
    	boolean  start = false;
    	if (data.contains(Constants.CMD_BUTTON_PRESS)) {
	        button = data.getUnsignedInteger(Constants.CMD_BUTTON_PRESS).intValue();
	        Log.d(TAG, "Constants.CMD_BUTTON_PRESS, button: " + button);
	        if (button == Constants.ORUXMAPS_START_RECORD_CONTINUE_PRESS) {
	            OruxMaps.startRecordNewSegment(context);
	        } else if (button == Constants.ORUXMAPS_STOP_RECORD_PRESS) {
	            OruxMaps.stopRecord(context);
	        } else if (button == Constants.ORUXMAPS_NEW_WAYPOINT_PRESS) {
                OruxMaps.newWaypoint(context);
	        } else {
	            start = true;
	        }
    	}    	
        if (data.contains(Constants.MSG_VERSION_PEBBLE)) {
            version = data.getInteger(Constants.MSG_VERSION_PEBBLE).intValue();
            Log.d(TAG, "Constants.MSG_VERSION_PEBBLE, version: " + version);
            start = true;
        }       
        /*if (data.contains(Constants.MSG_LIVE_ASK_NAMES)) {
            live_max_name = data.getInteger(Constants.MSG_LIVE_ASK_NAMES).intValue();
            Log.d(TAG, "Constants.MSG_LIVE_ASK_NAMES, live_max_name: " + live_max_name);
            start = true;
        }*/

    	PebbleKit.sendAckToPebble(context, transactionId);
    	
        if (start) {
        	Intent i = new Intent(context, MainActivity.class);
            i.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK   // If set, this activity will become the start of a new task on this history stack
                  | Intent.FLAG_ACTIVITY_CLEAR_TOP  // If set, and the activity being launched is already running in the current task, then instead of launching a new instance of that activity, all of the other activities on top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent
                  | Intent.FLAG_ACTIVITY_SINGLE_TOP // If set, the activity will not be launched if it is already running at the top of the history stack
            );
            
        	if (button >= 0) {
    	        i.putExtra("button", button);
        	}
            if (version >= 0) {
                i.putExtra("version", version);
            }
            /*if (live_max_name != -99) {
                i.putExtra("live_max_name", live_max_name);
            }*/

        	context.startActivity(i);
        }
    }
}