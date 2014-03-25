package com.njackson;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: njackson
 * Date: 22/05/2013
 * Time: 17:26
 * To change this template use File | Settings | File Templates.
 */

public class Constants {
    public static final UUID WATCH_UUID = java.util.UUID.fromString("5dd35873-3bb6-44d6-8255-0e61bc3b97f5");

    public static final int VERSION_ANDROID = 15;
    public static final int LAST_VERSION_PEBBLE = 23;
    public static final int MIN_VERSION_PEBBLE = 22;

    public static final int LIVE_TRACKING_FRIENDS = 0x10;
    public static final int PEBBLE_LOCTATION_DATA = 0x13;
    public static final int STATE_CHANGED = 0x14;
    public static final int MSG_VERSION_PEBBLE = 0x15;
    public static final int MSG_VERSION_ANDROID = 0x16;
    public static final int MSG_LIVE_SHORT = 0x17;

    public static final int MSG_LIVE_NAME0 = 0x19;
    public static final int MSG_LIVE_NAME1 = 0x20;
    public static final int MSG_LIVE_NAME2 = 0x21;
    public static final int MSG_LIVE_NAME3 = 0x22;
    public static final int MSG_LIVE_NAME4 = 0x23;
    public static final int MSG_BATTERY_LEVEL = 0x24;

    public static final String PREFS_NAME = "PebbleBikePrefs";

    public static final int PLAY_PRESS = 0x0;
    public static final int STOP_PRESS = 0x1;
    public static final int REFRESH_PRESS = 0x2;
    public static final int CMD_BUTTON_PRESS = 0x4;
    public static final int ORUXMAPS_START_RECORD_CONTINUE_PRESS = 0x5;
    public static final int ORUXMAPS_STOP_RECORD_PRESS = 0x6;
    public static final int ORUXMAPS_NEW_WAYPOINT_PRESS = 0x7;

    public static final int STATE_STOP = 0x0;
    public static final int STATE_START = 0x1;

    public static final int IMPERIAL = 0x0;
    public static final int METRIC = 0x1;

    public static final double MS_TO_KPH = 3.6;
    public static final double MS_TO_MPH = 2.23693629;
    public static final double M_TO_KM = 0.001;
    public static final double M_TO_MILES = 0.000621371192;
    public static final double M_TO_M = 1;
    public static final double M_TO_FEET = 3.2808399;
}

