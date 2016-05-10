package com.carconnectivity.mlmediaplayer.utils;

import android.util.Log;

import com.carconnectivity.mlmediaplayer.mediabrowser.events.AnimateAlphaEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProgressUpdateEvent;
import com.carconnectivity.mlmediaplayer.utils.event.RockScoutEvent;
import de.greenrobot.event.EventBus;

public class RsEventBus {

    private static final String TAG = RsEventBus.class.getSimpleName();

    private static final EventBus mBus = new EventBus();

    public static void register(Object subscriber) {
        if (!mBus.isRegistered(subscriber)) {
            mBus.register(subscriber);
        }
    }

    public static void registerSticky(Object subscriber) {
        if (!mBus.isRegistered(subscriber)) {
            mBus.registerSticky(subscriber);
        }
    }

    public static void post(RockScoutEvent event) {
        if (event instanceof AnimateAlphaEvent == false &&
                event instanceof ProgressUpdateEvent == false) {
            Log.d(TAG, "Post event: " + event.getClass().getSimpleName() + " - " + event);
        }
        mBus.post(event);
    }

    public static void postSticky(RockScoutEvent event) {
        Log.d(TAG, "Post sticky event: " + event.getClass().getSimpleName() + " - " + event);
        mBus.postSticky(event);
    }

    public static void unregister(Object subscriber) {
        mBus.unregister(subscriber);
    }
}
