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

package com.carconnectivity.mlmediaplayer.commonapi;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;

import com.carconnectivity.mlmediaplayer.commonapi.events.AudioBlockingEvent;
import com.carconnectivity.mlmediaplayer.commonapi.events.AudioContextChangedEvent;
import com.carconnectivity.mlmediaplayer.commonapi.events.ConnectionMirrorLinkServiceEvent;
import com.carconnectivity.mlmediaplayer.commonapi.events.DriveModeStatusChangedEvent;
import com.carconnectivity.mlmediaplayer.commonapi.events.MirrorLinkSessionChangedEvent;
import com.carconnectivity.mlmediaplayer.commonapi.events.PlaybackFailedEvent;
import com.carconnectivity.mlmediaplayer.commonapi.events.PrepareForPlaybackEvent;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;
import com.mirrorlink.android.commonapi.Defs;
import com.mirrorlink.android.commonapi.IConnectionListener;
import com.mirrorlink.android.commonapi.IConnectionManager;
import com.mirrorlink.android.commonapi.IContextListener;
import com.mirrorlink.android.commonapi.IContextManager;
import com.mirrorlink.android.commonapi.IDeviceStatusListener;
import com.mirrorlink.android.commonapi.IDeviceStatusManager;


/**
 * Responsible for connection with MirrorLink, handling blocks
 * and updating state of player to head unit.
 */

public final class MirrorLinkConnectionManager {
    public final static String TAG = MirrorLinkConnectionManager.class.getSimpleName();

    private final int currentCategory = Defs.ContextInformation.APPLICATION_CATEGORY_MEDIA_MUSIC;
    private MirrorLinkApplicationContext mMirrorLinkApplicationContext;

    private Handler mHandler;

    private volatile boolean mMirrorLinkActive = false;
    private volatile boolean mPlaybackStatus = false;
    private volatile boolean mInDriveMode = false;
    public static volatile boolean mIsMirrorLinkSupported = false;

    private enum States {
        STATE_STOP(1),
        STATE_PLAY(3),
        STATE_PAUSE(2);
        private final int value;

        States(int value) {
            this.value = value;
        }

        public final int getValue() {
            return value;
        }
    }

    final SparseArray<States> STATES = new SparseArray<States>() {{
        put(1, States.STATE_STOP);
        put(2, States.STATE_PAUSE);
        put(3, States.STATE_PLAY);
    }};

    public MirrorLinkConnectionManager(MirrorLinkApplicationContext applicationContext, Handler handler) {
        mHandler = handler;
        mMirrorLinkApplicationContext = applicationContext;
        RsEventBus.register(this);
        if (mMirrorLinkApplicationContext.getService() == null) {
            if (!mMirrorLinkApplicationContext.connect()) {             // correct -> !mMirrorLinkApplicationContext.connect()
                RsEventBus.postSticky(new MirrorLinkSessionChangedEvent(mMirrorLinkActive));
            }
        }
    }

    public void disconnectFromApiService() {
        unregisterMirrorLinkManagers();
        if (mIsMirrorLinkSupported)
            mMirrorLinkApplicationContext.disconnect();
    }

    public void registerMirrorLinkManagers() {
        Log.d(TAG, "Registering MirrorLinkManagers");
        RsEventBus.register(this);
        if (mMirrorLinkApplicationContext.getService() != null) {
            try {
                mMirrorLinkApplicationContext.registerDeviceStatusManager(this, mDeviceStatusListener);
                mMirrorLinkApplicationContext.registerConnectionManager(this, mConnectionListener);
                mMirrorLinkApplicationContext.registerContextManager(this, mContextListener);

                final IConnectionManager connectionManager
                        = mMirrorLinkApplicationContext.getConnectionManager();
                if (connectionManager != null) {
                    Log.d(TAG, "ConnectionManager available");
                    setMirrorLinkConnected(connectionManager.isMirrorLinkSessionEstablished());
                }

                final IDeviceStatusManager deviceStatusManager
                        = mMirrorLinkApplicationContext.getDeviceStatusManager();
                if (deviceStatusManager != null) {
                    mInDriveMode = deviceStatusManager.isInDriveMode();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            RsEventBus.postSticky(new DriveModeStatusChangedEvent(mInDriveMode));
                        }
                    });
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void unregisterMirrorLinkManagers() {
        Log.d(TAG, "Unregistering MirrorLinkManagers");
        RsEventBus.unregister(this);
        if (mMirrorLinkApplicationContext != null) {
            mMirrorLinkApplicationContext.unregisterDeviceStatusManager(this, mDeviceStatusListener);
            mMirrorLinkApplicationContext.unregisterConnectionManager(this, mConnectionListener);
            mMirrorLinkApplicationContext.unregisterContextManager(this, mContextListener);
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ConnectionMirrorLinkServiceEvent event) {
        if (event.isConnected) {
            registerMirrorLinkManagers();
        } else {
            unregisterMirrorLinkManagers();
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(PrepareForPlaybackEvent event) {
        /* This should force setting audio context before any actual playback is done. */
        updatePlaybackStatus(true);
    }

    @SuppressWarnings("unused")
    public void onEvent(PlaybackFailedEvent event) {
        /* This should remove audio context if playback failed,
         * audio context was previously set by PrepareForPlaybackEvent. */
        updatePlaybackStatus(false);
    }

    @SuppressWarnings("unused")
    public void onEvent(AudioContextChangedEvent event) {
        Log.d(TAG, "mIsMirrorLinkSupported:" + mIsMirrorLinkSupported + ", mMirrorLinkActive:" + mMirrorLinkActive);
        if (!mIsMirrorLinkSupported) return;
        if (!mMirrorLinkActive) return;

        States playbackStatus = STATES.get(event.state.state);
        if (playbackStatus == null) return;

        boolean newStatus;
        switch (playbackStatus) {
            case STATE_PLAY:
                newStatus = true;
                break;

            default:
                newStatus = false;
                break;
        }
        updatePlaybackStatus(newStatus);
    }

    private void setAudioContext(boolean isPlaying) {
        Log.d(TAG, "setAudioContext isPlaying" + isPlaying);
        try {
            final IContextManager manager = mMirrorLinkApplicationContext.getContextManager();
            if (manager != null) {
                final int[] categories = {currentCategory};
                manager.setAudioContextInformation(isPlaying, categories, true);
            }
        } catch (RemoteException re) {
            Log.e(TAG, re.getMessage());
        }
    }

    void setMirrorLinkConnected(boolean connected) {
        Log.d(TAG, "setMirrorLinkConnected connected:" + connected);
        if (mIsMirrorLinkSupported) {
            mMirrorLinkActive = connected;
            RsEventBus.postSticky(new MirrorLinkSessionChangedEvent(mMirrorLinkActive));
        }
    }

    IConnectionListener mConnectionListener = new IConnectionListener.Stub() {
        @Override
        public void onRemoteDisplayConnectionChanged(int remoteDisplayConnection) throws RemoteException {
        }

        @Override
        public void onMirrorLinkSessionChanged(final boolean mirrorLinkSessionIsEstablished) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setMirrorLinkConnected(mirrorLinkSessionIsEstablished);
                }
            });

            setAudioContext(mPlaybackStatus);
        }

        @Override
        public void onAudioConnectionsChanged(Bundle audioConnections) throws RemoteException {
        }
    };

    IDeviceStatusListener mDeviceStatusListener = new IDeviceStatusListener.Stub() {
        @Override
        public void onDriveModeChange(boolean driveMode) throws RemoteException {
            mInDriveMode = driveMode;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    RsEventBus.postSticky(new DriveModeStatusChangedEvent(mInDriveMode));
                }
            });

        }

        @Override
        public void onNightModeChanged(boolean nightMode) throws RemoteException {
        }

        @Override
        public void onMicrophoneStatusChanged(boolean micInput) throws RemoteException {
        }
    };
    IContextListener mContextListener = new IContextListener.Stub() {
        @Override
        public void onAudioBlocked(final int reason) throws RemoteException {
            Log.d(TAG, "onAudioBlocked");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    RsEventBus.postSticky(new AudioBlockingEvent(true));
                }
            });
        }

        @Override
        public void onFramebufferBlocked(int reason, Bundle framebufferArea) throws RemoteException {
        }

        @Override
        public void onFramebufferUnblocked() throws RemoteException {
        }

        @Override
        public void onAudioUnblocked() throws RemoteException {
            Log.d(TAG, "onAudioUnblocked");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    RsEventBus.postSticky(new AudioBlockingEvent(false));
                }
            });
        }
    };

    private void updatePlaybackStatus(boolean playbackStatus) {
        Log.d(TAG, "updatePlaybackStatus playbackStatus:" + playbackStatus);
        mPlaybackStatus = playbackStatus;
        setAudioContext(playbackStatus);
    }
}
