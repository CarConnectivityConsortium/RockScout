package com.carconnectivity.mlmediaplayer.mediabrowser.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.carconnectivity.mlmediaplayer.mediabrowser.events.RefreshProvidersEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.TerminateEvent;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;

/**
 * Created by sebastian.sokolowski on 01.06.16.
 */
public class CancelReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        RsEventBus.postSticky(new TerminateEvent());
        RsEventBus.postSticky(new RefreshProvidersEvent());
    }
}
