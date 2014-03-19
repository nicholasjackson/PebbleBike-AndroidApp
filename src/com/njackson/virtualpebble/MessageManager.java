package com.njackson.virtualpebble;

import com.njackson.interfaces.IMessageManager;

/**
 * Created by server on 18/03/2014.
 */
public class MessageManager implements IMessageManager {
    @Override
    public String doStuff() {
        return "Stuff";
    }
}
