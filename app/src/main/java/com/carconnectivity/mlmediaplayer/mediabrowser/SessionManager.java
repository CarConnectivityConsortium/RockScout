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
import android.content.pm.PackageManager;
import android.util.Log;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.*;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;

/**
 * Created by belickim on 20/04/15.
 */
public class SessionManager {
    private static final String TAG = SessionManager.class.getSimpleName();
    private final ProvidersManager mManger;

    /**
     * the provider that is now browsed
     */
    private Provider mBrowsedProvider;
    /**
     * the provider that is now playing
     */
    private Provider mPlayingProvider;


    public SessionManager(Context context, PackageManager packageManager) {
        mManger = new ProvidersManager(context, packageManager);

        RsEventBus.register(this);
    }

    public void findProviders() {
        Log.d(TAG,"find providers");
        mManger.findProviders();
    }

    public boolean hasBrowsedProvider() {
        return mBrowsedProvider != null && mBrowsedProvider.isConnected();
    }

    public ProviderView getNowPlayingProviderView() {
        if (mPlayingProvider == null) return null;
        return mManger.getProviderView(mPlayingProvider.getName());
    }

    public ProviderView getBrowsedProviderView() {
        if (mBrowsedProvider == null) return null;
        return mManger.getProviderView(mBrowsedProvider.getName());
    }

    public void refreshProviders() {
        Log.d(TAG,"refresh providers");
        mManger.refreshProviders();
    }

    public boolean isPlayingProvider(ComponentName name) {
        return mPlayingProvider != null && mPlayingProvider.isNameEqual(name);
    }

    @SuppressWarnings("unused")
    public void onEvent(PlayMediaItemEvent event) {
        changePlayingProvider(mBrowsedProvider);
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderConnectedEvent event) {
        if(event.provider != null) return;
        if(!event.cleanPlayingProvider) return;

        RsEventBus.postSticky(new NowPlayingProviderChangedEvent(null));
        mPlayingProvider = null;
    }

    @SuppressWarnings("unused")
    public void onEvent(StartBrowsingEvent event) {
        final ComponentName name = event.provider.getUniqueName();
        final boolean wasConnectedBefore = mManger.isConnected(name);

        changeBrowsedProvider(name, false);
        if (wasConnectedBefore) {
            /* if already connected manually browse root directory: */
            final ComponentName currentName = mBrowsedProvider.getName();
            final BrowseDirectoryEvent browseEvent = new BrowseDirectoryEvent(currentName, null);
            RsEventBus.post(browseEvent);
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderDiscoveredEvent event) {
        if (event.isPlaying && mPlayingProvider == null) {
            Log.d(TAG,"ProviderDiscoveredEvent");
            changeBrowsedProvider(event.provider.getUniqueName(), true);
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(DisconnectFromCurrentProviderEvent event) {
        disconnect();
    }

    @SuppressWarnings("unused")
    public void onEvent(DisableEventsEvent event){
        RsEventBus.unregister(this);
    }

    private void changePlayingProvider(Provider maybePlayingProvider) {
        if (maybePlayingProvider == null) return;

        final ComponentName newName = maybePlayingProvider.getName();
        if (isPlayingProvider(newName)) return;


        if (mPlayingProvider != null && mPlayingProvider.isPlaying()) mPlayingProvider.forcePause();
        mPlayingProvider = maybePlayingProvider;
        RsEventBus.postSticky(new NowPlayingProviderChangedEvent(mPlayingProvider.getView()));
    }

    private void changePlayingProviderIfPlaying(Provider maybePlayingProvider) {
        if (maybePlayingProvider == null) return;
        if (!maybePlayingProvider.isPlaying()) return;
        if (mPlayingProvider != null && mPlayingProvider.isPlayingOrPreparing()) return;
        Log.d(TAG,"changePlayingProviderIfPlaying");
        changePlayingProvider(maybePlayingProvider);
    }

    public void changeBrowsedProvider(ComponentName providerName, boolean showPlayer) {
        disconnectBrowsedProvider(false);

        mBrowsedProvider = mManger.getProvider(providerName);
        RsEventBus.postSticky(new CurrentlyBrowsedProviderChanged(mBrowsedProvider.getView()));
        if (!mBrowsedProvider.isConnected()) {
            mBrowsedProvider.connect(showPlayer);
        }

        if (mPlayingProvider == null) {
            Log.d(TAG,"changeBrowsedProvider");
            changePlayingProvider(mBrowsedProvider);
        }
    }

    private void disconnectBrowsedProvider(boolean unconditionalDisconnect) {
        if (mBrowsedProvider == null) return;
        if (!mBrowsedProvider.isConnected()) return;

        // don't disconnect when browserProvider is equal playingProvider
        if (unconditionalDisconnect || mPlayingProvider != null && !mBrowsedProvider.isNameEqual(mPlayingProvider.getName())) {
            Log.d(TAG, "disconnectBrowsedProvider");
            mBrowsedProvider.disconnect(false);
        }
    }

    private void disconnectPlayingProvider() {
        if (mPlayingProvider == null) return;
        if (!mPlayingProvider.isConnected()) return;

        Log.d(TAG, "disconnectPlayingProvider");
        mPlayingProvider.disconnect(false);
    }

    public void disconnect() {
        disconnectBrowsedProvider(true);
        disconnectPlayingProvider();
    }

    private void tryReconnectProvider(Provider provider) {
        if (provider == null) {
            Log.w(TAG, "Cannot reconnect, current provider is null");
            return;
        }
        if (provider.isConnected()) {
            provider.disconnect(false);
        }
        provider.connect(false);
    }

    public void tryReconnect() {
        tryReconnectProvider(mPlayingProvider);
        tryReconnectProvider(mBrowsedProvider);
    }

    public void changeModePlayer(boolean mPlayerModeOnline) {
        mManger.changeModePlayer(mPlayerModeOnline);

        //playing provider
        if(mPlayingProvider != null) {
            if(!mPlayerModeOnline && !mPlayingProvider.canPlayOffline()) {
                //pause playing and disconnect
                if (mPlayingProvider.isPlaying()) mPlayingProvider.forcePause();
                mPlayingProvider.disconnect(true);
            }
        }

        //browsing provider
        if(mBrowsedProvider != null) {
            if(mPlayingProvider.getName().equals(mBrowsedProvider.getName())) return;
            if(!mPlayerModeOnline && !mBrowsedProvider.canPlayOffline()) {
                RsEventBus.post(new ProviderBrowseCancelEvent());
                mBrowsedProvider = null;
            }
        }
    }
}
