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
 * Car Connectivity Consortium products ("Feedback").
 *
 * You agrees that any such Feedback is given on non-confidential
 * basis and Licensee hereby waives any confidentiality restrictions
 * for such Feedback. In addition, Licensee grants to the Car Connectivity Consortium
 * and its affiliates a worldwide, non-exclusive, perpetual, irrevocable,
 * sub-licensable, royalty-free right and license under Licensee's copyrights to copy,
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
import android.util.Log;

import com.carconnectivity.mlmediaplayer.mediabrowser.events.BrowseDirectoryEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.DisconnectFromCurrentProviderEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderBrowseErrorEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderConnectErrorEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderBrowseSuccessfulEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderConnectedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderDiscoveredEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.TrackMetadata;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by belickim on 16/04/15.
 */
final class Provider {
    public final static String TAG = Provider.class.getSimpleName();

    final private ProvidersManager mManager;
    final private ComponentName mName;

    private boolean mConnected;
    private boolean mCanConnect;

    private ConnectionCallback mActiveConnectionCallback;
    private final ProviderMediaController mMediaController;

    private EventBus mBus = EventBus.getDefault();

    public Provider(ProvidersManager manager, ResolveInfo packageInfo) {
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

        mMediaController = new ProviderMediaController(this);
    }

    public boolean isConnected() { return mConnected; }
    public boolean canConnect() { return mCanConnect; }
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

    public void disconnect() {
        if (mActiveConnectionCallback == null) {
            throw new NullPointerException("Unexpected state: active connection callback is null.");
        }

        mActiveConnectionCallback.disconnect();
        mBus.post(new ProviderConnectedEvent(null, false));
        mActiveConnectionCallback = null;
        mConnected = false;
    }

    private void connectWithCallback(ConnectionCallbackBase callback) {
        final MediaBrowser browser = createBrowser(callback);
        callback.setBrowser(browser);
        callback.setProvider(this);
        browser.connect();
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
        @Override
        public void onConnected() {
            mCanConnect = true;
            mConnected = true;
            final boolean isPlaying = checkIsPlaying();
            /* possibility to make connection has been tested, disconnect */
            getBrowser().disconnect();
            mConnected = false;
            final ProviderView view = mManager.getProviderView(mName);
            mBus.post(new ProviderDiscoveredEvent(view, isPlaying));
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
            final ProviderView view = mManager.getProviderView(mName);
            mBus.post(new ProviderDiscoveredEvent(view, false));
        }

        @Override
        public void onConnectionSuspended() {
            mBus.post(new ProviderConnectedEvent(null, false));
            mBus.post(new DisconnectFromCurrentProviderEvent());
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
        private final boolean mShowPlayer;
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
                mBus.postSticky(event);
            }

            @Override
            public void onError(String id) {
                Log.d(TAG, "Browsing failed: " + id);
                mBus.postSticky(new ProviderBrowseErrorEvent(id));
            }
        };

        public ConnectionCallback(boolean showPlayer) {
            super();
            mShowPlayer = showPlayer;
        }

        public void disconnect() {
            getBrowser().disconnect();
            mMediaController.stopListening();
            mConnected = false;
            mBus.unregister(this);
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
                mBus.register(this);

                final ProviderView view = mManager.getProviderView(mName);
                mBus.postSticky(new ProviderConnectedEvent(view, mShowPlayer));

                mConnected = true;
            } else {
                Log.d(TAG, "Connection failed to acquire token: " + mName.toString());
                Provider.this.disconnect();
                Provider.this.connect(mShowPlayer);
                mBus.postSticky(new ProviderConnectErrorEvent(mName.toString()));
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
            mBus.post(new ProviderConnectedEvent(null, false));
            mBus.post(new DisconnectFromCurrentProviderEvent());
        }
    }
}
