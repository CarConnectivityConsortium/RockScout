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
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.*;
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

    private boolean mConnected;
    private boolean mCanConnect;
    private boolean mCanPlayOffline; /* offline = without ML session */

    private ConnectionCallback mActiveConnectionCallback;
    private final ProviderMediaController mMediaController;

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

    public boolean isConnected() { return mConnected; }
    public boolean canConnect() { return mCanConnect; }
    public boolean canPlayOffline() { return mCanPlayOffline; }
    public ComponentName getName() { return mName; }

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

    public ProviderView getView() {
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
        if (mConnected) {
            throw new IllegalStateException("Provider is already connected.");
        }
        mActiveConnectionCallback = new ConnectionCallback(showPlayer);
        connectWithCallback(mActiveConnectionCallback);
    }

    public void disconnect(boolean cleanProvider) {
        if (mActiveConnectionCallback == null) {
            throw new NullPointerException("Unexpected state: active connection callback is null.");
        }

        mActiveConnectionCallback.disconnect();
        RsEventBus.post(new ProviderConnectedEvent(null, false, cleanProvider));
        mActiveConnectionCallback = null;
        mConnected = false;
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

    private abstract class ConnectionCallbackBase extends MediaBrowser.ConnectionCallback {
        private MediaBrowser mBrowser = null;
        private Provider mProvider = null;

        public void setBrowser(MediaBrowser browser) { mBrowser = browser; }
        public MediaBrowser getBrowser() { return mBrowser; }

        public void setProvider(Provider browser) { mProvider = browser; }
        public Provider getProvider() { return mProvider; }
    }

    private class TestConnectionCallback extends ConnectionCallbackBase {
        private final int MAX_RETRY_CONNECT = 20;
        private final int TIME_BEFORE_RECONNECT = 100;
        private boolean mRetryConnect;
        private int mCountRetryConnect;

        public TestConnectionCallback() {
            super();
            mRetryConnect = false;
            mCountRetryConnect = 0;
        }

        @Override
        public void onConnected() {
            final MediaBrowser browser = getBrowser();
            final MediaSession.Token token = browser.getSessionToken();
            if(token != null) {
                mCanConnect = true;
                mConnected = true;
                final ProviderView view = mManager.getProviderView(mName);
                final boolean isPlaying = checkIsPlaying();
            /* possibility to make connection has been tested, disconnect */
                getBrowser().disconnect();
                mConnected = false;
                mManager.addConnectedProvider(view.getUniqueName(), isPlaying);
            } else {
                mRetryConnect = true;
                mCountRetryConnect++;
                Log.d(TAG, "Test connection failed to acquire token: " + mName.toString());
                disconnect();
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
            mCanConnect = false;
        }

        @Override
        public void onConnectionSuspended() {
            RsEventBus.post(new ProviderConnectedEvent(null, false, false));
            RsEventBus.post(new DisconnectFromCurrentProviderEvent());
        }

        private void disconnect() {
            getBrowser().disconnect();
            mMediaController.stopListening();
            mConnected = false;
            if (mRetryConnect) {
                if (mCountRetryConnect >= MAX_RETRY_CONNECT) {
                    final ProviderView view = mManager.getProviderView(mName);
                    RsEventBus.post(new ProviderDiscoveredEvent(view, false));
                } else {
                    Log.d(TAG, "Retry provider test connection, count retry:" + mCountRetryConnect);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getBrowser().connect();
                        }
                    }, TIME_BEFORE_RECONNECT);
                }
            }
        }
    }

    private ArrayList<MediaItemView> convertToViews(List<MediaBrowser.MediaItem> children) {
        ArrayList<MediaItemView> views = new ArrayList<>(children.size());
        for (int i = 0; i < children.size(); i++) {
            views.add(new MediaItemView(children.get(i)));
        }
        return views;
    }

    private class ConnectionCallback extends ConnectionCallbackBase {
        private final int MAX_RETRY_CONNECT = 20;
        private final int TIME_BEFORE_RECONNECT = 300;
        private final boolean mShowPlayer;
        private boolean mRetryConnect;
        private int mCountRetryConnect;
        MediaController mController;
        String mLastSubscription;
        MediaBrowser.SubscriptionCallback mSubscriptionCallback
                = new MediaBrowser.SubscriptionCallback() {
            @Override
            public void onChildrenLoaded(String parentId, List<MediaBrowser.MediaItem> children) {
                super.onChildrenLoaded(parentId, children);

                ArrayList<MediaItemView> views = convertToViews(children);
                final ProviderBrowseSuccessfulEvent event
                        = new ProviderBrowseSuccessfulEvent(getView(), parentId, views);
                RsEventBus.postSticky(event);
            }

            @Override
            public void onError(String id) {
                Log.d(TAG, "Browsing failed: " + id);
                RsEventBus.postSticky(new ProviderBrowseErrorEvent(id));
            }
        };

        public ConnectionCallback(boolean showPlayer) {
            super();
            mRetryConnect = false;
            mCountRetryConnect = 0;
            mShowPlayer = showPlayer;
        }

        public void disconnect() {
            getBrowser().disconnect();
            mMediaController.stopListening();
            mConnected = false;
            RsEventBus.unregister(this);
            if (mRetryConnect) {
                if (mCountRetryConnect >= MAX_RETRY_CONNECT) {
                    RsEventBus.postSticky(new ProviderConnectErrorEvent(mName.toString()));
                } else {
                    Log.d(TAG, "Retry provider connection, count retry:" + mCountRetryConnect);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getBrowser().connect();
                        }
                    }, TIME_BEFORE_RECONNECT);
                }
            }
        }

        @Override
        public void onConnected() {
            final MediaBrowser browser = getBrowser();
            final MediaSession.Token token = browser.getSessionToken();
            if (token != null) {
                mController = new MediaController(mManager.getContext(), token);
                mMediaController.startListening(mManager.getContext(), mController);

                final String root = browser.getRoot();
                mLastSubscription = root;
                browser.subscribe(root, mSubscriptionCallback);
                RsEventBus.register(this);

                final ProviderView view = mManager.getProviderView(mName);
                RsEventBus.postSticky(new ProviderConnectedEvent(view, mShowPlayer, false));

                mConnected = true;
                mRetryConnect = false;
                mCountRetryConnect = 0;
            } else {
                mRetryConnect = true;
                mCountRetryConnect++;
                Log.d(TAG, "Connection failed to acquire token: " + mName.toString());
                disconnect();
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
            mConnected = false;
            mMediaController.stopListening();
        }

        @Override
        public void onConnectionSuspended() {
            mMediaController.stopListening();
            RsEventBus.post(new ProviderConnectedEvent(null, false, false));
            RsEventBus.post(new DisconnectFromCurrentProviderEvent());
        }
    }
}
