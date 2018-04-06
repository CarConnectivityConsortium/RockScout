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

package com.carconnectivity.mlmediaplayer.mediabrowser;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.carconnectivity.mlmediaplayer.mediabrowser.events.BrowseDirectoryEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.DisconnectFromProviderEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderBrowseErrorEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderBrowseSuccessfulEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderConnectErrorEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderConnectedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.TrackMetadata;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by belickim on 16/04/15.
 */
final class Provider {
    public final static String TAG = Provider.class.getSimpleName();

    final private ProvidersManager mManager;
    final private ComponentName mName;
    private final ProviderMediaController mMediaController;
    private boolean mConnected;
    private boolean mCanConnect;
    private boolean mCanPlayOffline; /* offline = without ML session */
    private ConnectionCallback mActiveConnectionCallback;

    public Provider(ProvidersManager manager, ResolveInfo packageInfo, boolean canPlayOffline) {
        if (manager == null) {
            throw new IllegalArgumentException("Manager cannot be null.");
        }
        if (packageInfo == null) {
            throw new IllegalArgumentException("Package info cannot be null.");
        }

        final ServiceInfo serviceInfo = packageInfo.serviceInfo;
        mManager = manager;
        mName = new ComponentName(serviceInfo.applicationInfo.packageName, serviceInfo.name);
        mConnected = false;
        mCanConnect = false;
        mCanPlayOffline = canPlayOffline;

        mMediaController = new ProviderMediaController(this);
    }

    public boolean isConnected() {
        return mConnected;
    }

    public boolean canConnect() {
        return mCanConnect;
    }

    public boolean canPlayOffline() {
        return mCanPlayOffline;
    }

    public ComponentName getName() {
        return mName;
    }

    public boolean isNameEqual(ComponentName name) {
        return name != null && mName.equals(name);
    }

    public boolean isPlaying() {
        return mMediaController.isPlaying();
    }

    public boolean isPlayingOrPreparing() {
        return mMediaController.isPlayingOrPreparing();
    }

    public TrackMetadata getCurrentMetadata() {
        return mMediaController.getCurrentMetadata();
    }

    public ProviderPlaybackState getPlaybackState() {
        return mMediaController.getPlaybackState();
    }

    public ProviderViewActive getView() {
        return mManager.getProviderView(mName);
    }

    public void forcePause() {
        mMediaController.forcePause();
    }

    public void testConnection() {
        if (mConnected) {
            throw new IllegalStateException("Provider is already connected.");
        }
        connectWithCallback(new TestConnectionCallback());
    }

    public void connect(boolean showPlayer) {
        Log.d(TAG, "Connect to provider: " + mName);
        if (!mConnected) {
            mActiveConnectionCallback = new ConnectionCallback(showPlayer);
            connectWithCallback(mActiveConnectionCallback);
        }
    }

    public void disconnect() {
        Log.d(TAG, "Disconnect from provider: " + mName);
        if (mActiveConnectionCallback != null) {
            mActiveConnectionCallback.disconnect();
            mActiveConnectionCallback = null;
            mConnected = false;
        }
    }

    private void connectWithCallback(final ConnectionCallbackBase callback) {
        final Provider provider = this;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final MediaBrowser browser = createBrowser(callback);
                callback.setBrowser(browser);
                callback.setProvider(provider);
                browser.connect();
            }
        });
    }

    private MediaBrowser createBrowser(MediaBrowser.ConnectionCallback callback) {
        return new MediaBrowser(mManager.getContext(), mName, callback, null);
    }

    private ArrayList<MediaItemView> convertToViews(List<MediaBrowser.MediaItem> children) {
        ArrayList<MediaItemView> views = new ArrayList<>();
        for (MediaBrowser.MediaItem mediaItem : children
                ) {
            if (mediaItem != null) {
                views.add(new MediaItemView(mediaItem));
            }
        }
        return views;
    }

    private abstract class ConnectionCallbackBase extends MediaBrowser.ConnectionCallback {
        private final int MAX_RECONNECT = 20;
        private final int TIME_BEFORE_RECONNECT = 250;
        private int mCountReconnect;

        private MediaBrowser mBrowser = null;
        private Provider mProvider = null;
        private boolean mTestConnection;

        public ConnectionCallbackBase(boolean testConnection) {
            mTestConnection = testConnection;
            mCountReconnect = 0;
        }

        public MediaBrowser getBrowser() {
            return mBrowser;
        }

        public void setBrowser(MediaBrowser browser) {
            mBrowser = browser;
        }

        public Provider getProvider() {
            return mProvider;
        }

        public void setProvider(Provider browser) {
            mProvider = browser;
        }

        private void reconnect() {
            getBrowser().disconnect();
            mMediaController.stopListening();
            mConnected = false;
            mCountReconnect++;
            Log.d(TAG, "Connection failed to acquire token: " + mName.toString());
            if (mCountReconnect >= MAX_RECONNECT) {
                if (mTestConnection) {
                    mManager.addTestedProvider(mName, false, false);
                } else {
                    RsEventBus.post(new ProviderConnectErrorEvent(mName.toString()));
                }

            } else {
                Log.d(TAG, "Retry provider connection, count retry: " + mCountReconnect);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getBrowser().connect();
                    }
                }, TIME_BEFORE_RECONNECT);
            }
        }
    }

    private class TestConnectionCallback extends ConnectionCallbackBase {

        public TestConnectionCallback() {
            super(true);
        }

        @Override
        public void onConnected() {
            Log.d(TAG, "TestConnectionCallback onConnected: " + mName);
            final MediaBrowser browser = getBrowser();

            MediaSession.Token token = null;
            try {
                token = browser.getSessionToken();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Lost connection with provider: ", e);
            }

            if (token != null) {
                mCanConnect = true;
                mConnected = true;
                final boolean isPlaying = checkIsPlaying();
            /* possibility to make connection has been tested, disconnect */
                getBrowser().disconnect();
                mConnected = false;
                mManager.addTestedProvider(mName, true, isPlaying);
            } else {
                super.reconnect();
            }
        }

        private boolean checkIsPlaying() {
            PlaybackState state = null;
            final MediaBrowser browser = getBrowser();
            if (browser != null && browser.getSessionToken() != null) {
                final MediaController controller
                        = new MediaController(mManager.getContext(), browser.getSessionToken());
                state = controller.getPlaybackState();
            }
            return state != null && PlaybackState.STATE_PLAYING == state.getState();
        }

        @Override
        public void onConnectionFailed() {
            Log.d(TAG, "TestConnectionCallback onConnectionFailed: " + mName);
            mCanConnect = false;
            mManager.addTestedProvider(mName, false, false);
        }

        @Override
        public void onConnectionSuspended() {
            Log.d(TAG, "TestConnectionCallback onConnectionSuspended: " + mName);
        }
    }

    private class ConnectionCallback extends ConnectionCallbackBase {
        private final boolean mShowPlayer;
        MediaController mController;
        String mLastSubscription;

        // Deezer not full children list -- workaround
        private boolean deezerFirstSubscription = true;
        private int deezerSubscribeCount = 0;
        private Handler deezerHandler = null;

        MediaBrowser.SubscriptionCallback mSubscriptionCallback
                = new MediaBrowser.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(String parentId, List<MediaBrowser.MediaItem> children) {
                super.onChildrenLoaded(parentId, children);

                ArrayList<MediaItemView> views = convertToViews(children);
                final ProviderBrowseSuccessfulEvent event
                        = new ProviderBrowseSuccessfulEvent(getView(), parentId, views);

                // Deezer not full children list -- workaround
                if (isDeezerAndFirstSubscription() && deezerSubscribeCount <= 5 && children.size() <= 2) {
                    if (deezerHandler == null) {
                        deezerHandler = new Handler();
                        deezerHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                deezerHandler = null;
                                getBrowser().subscribe(mLastSubscription, mSubscriptionCallback);
                                deezerSubscribeCount++;
                            }
                        }, 1000);
                    }
                } else {
                    deezerFirstSubscription = false;
                    deezerSubscribeCount = 0;

                    RsEventBus.post(event);
                }
            }

            @Override
            public void onError(String id) {
                Log.d(TAG, "Browsing failed: " + id);
                RsEventBus.post(new ProviderBrowseErrorEvent(id));
            }
        };

        public ConnectionCallback(boolean showPlayer) {
            super(false);
            mShowPlayer = showPlayer;
        }

        /**
         * Deezer not full children list -- workaround
         * <p>
         * Check internet connection improve exit from workaround as soon as possible, because
         * offline Deezer show only two children.
         *
         * @return
         */
        private boolean isDeezerAndFirstSubscription() {
            ConnectivityManager cm = (ConnectivityManager) mManager.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                if (deezerFirstSubscription && mName.getPackageName().equals("deezer.android.app")) {
                    return true;
                }
            }
            return false;
        }

        public void disconnect() {
            Log.d(TAG, "disconnect " + mName);
            final MediaBrowser browser = getBrowser();
            if (browser != null) {
                getBrowser().disconnect();
            }
            mMediaController.stopListening();
            mConnected = false;
            RsEventBus.unregister(this);
        }

        @Override
        public void onConnected() {
            final MediaBrowser browser = getBrowser();

            MediaSession.Token token = null;
            try {
                token = browser.getSessionToken();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Lost connection with provider: ", e);
            }

            if (token != null) {
                mController = new MediaController(mManager.getContext(), token);
                mMediaController.startListening(mManager.getContext(), mController);

                final String root = browser.getRoot();
                mLastSubscription = root;
                browser.subscribe(root, mSubscriptionCallback);
                RsEventBus.register(this);

                RsEventBus.post(new ProviderConnectedEvent(mName, mShowPlayer, false));

                mConnected = true;
            } else {
                super.reconnect();
            }
        }

        @SuppressWarnings("unused")
        public void onEvent(BrowseDirectoryEvent event) {
            if (mConnected == false) return;
            if (event.providerName.equals(mName) == false) return;

            final MediaBrowser browser = getBrowser();
            if (mLastSubscription != null) {
                browser.unsubscribe(mLastSubscription);
            }
            String directoryId = event.directoryId;
            if (directoryId == null) {
                directoryId = browser.getRoot();
            }
            browser.subscribe(directoryId, mSubscriptionCallback);
            mLastSubscription = directoryId;
        }

        @Override
        public void onConnectionFailed() {
            Log.d(TAG, "ConnectionCallback onConnectionFailed " + mName);
            mConnected = false;
            deezerFirstSubscription = true;
            deezerSubscribeCount = 0;
            mMediaController.stopListening();
            RsEventBus.post(new DisconnectFromProviderEvent(mName));
        }

        @Override
        public void onConnectionSuspended() {
            Log.d(TAG, "ConnectionCallback onConnectionSuspended " + mName);
            mConnected = false;
            deezerFirstSubscription = true;
            deezerSubscribeCount = 0;
            mMediaController.stopListening();
            RsEventBus.post(new DisconnectFromProviderEvent(mName));
        }
    }
}
