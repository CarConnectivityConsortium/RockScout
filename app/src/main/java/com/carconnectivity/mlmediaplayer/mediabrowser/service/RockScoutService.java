/*
 * Copyright Car Connectivity Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You may decide to give the Car Connectivity Consortium input, suggestions
 * or feedback of a technical nature which may be implemented on the
 * Car Connectivity Consortium products (“Feedback”).
 *
 * You agrees that any such Feedback is given on non-confidential
 * basis and Licensee hereby waives any confidentiality restrictions
 * for such Feedback. In addition, Licensee grants to the Car Connectivity Consortium
 * and its affiliates a worldwide, non-exclusive, perpetual, irrevocable,
 * sub-licensable, royalty-free right and license under Licensee’s copyrights to copy,
 * reproduce, modify, create derivative works and directly or indirectly
 * distribute, make available and communicate to public the Feedback
 * in or in connection to any CCC products, software and/or services.
 */

package com.carconnectivity.mlmediaplayer.mediabrowser.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.commonapi.MirrorLinkApplicationContext;
import com.carconnectivity.mlmediaplayer.commonapi.MirrorLinkConnectionManager;
import com.carconnectivity.mlmediaplayer.commonapi.events.MirrorLinkSessionChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.SessionManager;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ClearLauncherList;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.DisableEventsEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.FinishActivityEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderDiscoveryFinished;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.RefreshProvidersEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ShowLauncherFragment;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.TerminateEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.receiver.CancelReceiver;
import com.carconnectivity.mlmediaplayer.ui.MainActivity;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;

/**
 * Created by sebastian.sokolowski on 21.03.16.
 */
public class RockScoutService extends Service {
    private final static String TAG = RockScoutService.class.getCanonicalName();
    private final static int NOTIFICATION_ID = 1212;
    private MirrorLinkConnectionManager mMirrorLinkConnectionManager;
    private SessionManager mManager;
    private Handler mHandler;

    private boolean mHeadUnitIsConnected = false;
    private boolean mTerminateReceived = false;
    private boolean mProviderDiscoveryFinished = false;
    private boolean mProviderDiscoveryStarted = false;

    public MirrorLinkApplicationContext getMirrorLinkApplicationContext() {
        return (MirrorLinkApplicationContext) getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        startNotification();

        mHandler = new Handler(Looper.getMainLooper());
        mMirrorLinkConnectionManager
                = new MirrorLinkConnectionManager(getMirrorLinkApplicationContext(), mHandler);
        mManager = new SessionManager(this, getPackageManager());

        RsEventBus.registerSticky(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (mProviderDiscoveryFinished) {
            RsEventBus.post(new ShowLauncherFragment(true));
            launcherRefreshApps();
        } else {
            if (!mProviderDiscoveryStarted) {
                mManager.findProviders();
                mProviderDiscoveryStarted = true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        RsEventBus.post(new FinishActivityEvent());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mMirrorLinkConnectionManager.disconnectFromApiService();
        RsEventBus.post(new DisableEventsEvent());
        RsEventBus.unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(MirrorLinkSessionChangedEvent event) {
        if (event.headUnitIsConnected) {
            mHeadUnitIsConnected = true;
        } else {
            mHeadUnitIsConnected = false;
        }
        launcherRefreshApps();
    }

    @SuppressWarnings("unused")
    public void onEvent(RefreshProvidersEvent event) {
        if (!mProviderDiscoveryFinished) return;
        if (!mTerminateReceived) {
            mManager.refreshProviders();
            mManager.tryConnectIfDisconnected();
        } else {
            exit();
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(TerminateEvent event) {
        mTerminateReceived = true;
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderDiscoveryFinished event) {
        mProviderDiscoveryStarted = false;
        mProviderDiscoveryFinished = true;

        launcherRefreshApps();

        if (mTerminateReceived) {
            exit();
        }
    }

    private void launcherRefreshApps() {
        RsEventBus.post(new ClearLauncherList());
        mManager.changeModePlayer(mHeadUnitIsConnected);
    }

    @SuppressWarnings("unused")
    public void onEvent(FinishActivityEvent event) {
        RsEventBus.post(new TerminateEvent());
        exit();
    }

    private void startNotification() {
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification);

        // set cancel action
        Intent cancelIntent = new Intent(this, CancelReceiver.class);
        PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(this, 0,
                cancelIntent, 0);
        contentView.setOnClickPendingIntent(R.id.cancel_button, pendingCancelIntent);

        // set open app action
        Intent openAppIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingOpenAppIntent = PendingIntent.getActivity(this, 0, openAppIntent, 0);
        contentView.setOnClickPendingIntent(R.id.title_notification, pendingOpenAppIntent);

        Notification.Builder builder =
                new Notification.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContent(contentView)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setVisibility(Notification.VISIBILITY_PUBLIC);

        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void exit(){
        stopSelf();
        RsEventBus.removeAllStickyEvents();
    }
}


