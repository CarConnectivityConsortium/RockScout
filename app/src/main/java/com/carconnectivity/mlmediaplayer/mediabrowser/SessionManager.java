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

import com.carconnectivity.mlmediaplayer.mediabrowser.events.BrowseDirectoryEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.CurrentlyBrowsedProviderChanged;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.DisableEventsEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.DisconnectFromProviderEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.NowPlayingProviderChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.PlayMediaItemEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderBrowseCancelEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderConnectedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderDiscoveredEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.StartBrowsingEvent;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;

/**
 * Created by belickim on 20/04/15.
 */
public class SessionManager {
    private static final String TAG = SessionManager.class.getSimpleName();
    private final ProvidersManager mManger;
    private Boolean mLastPlayerModeOnline;

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
        Log.d(TAG, "findProviders");
        mManger.findProviders();
    }

    public void refreshProviders() {
        Log.d(TAG, "refreshProviders");
        mManger.refreshProviders();
    }

    public boolean isPlayingProvider(ComponentName name) {
        return mPlayingProvider != null && mPlayingProvider.isNameEqual(name);
    }

    @SuppressWarnings("unused")
    public void onEvent(PlayMediaItemEvent event) {
        changePlayingProvider(mBrowsedProvider.getName());
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderConnectedEvent event) {
        if (event.componentName == null) return;
        if (!event.showPlayer) return;
        Provider provider = mManger.getProvider(event.componentName);
        changePlayingProvider(event.componentName);
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
        if (event.isPlaying) {
            Provider provider = mManger.getProvider(event.provider.getUniqueName());
            provider.connect(true);
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(DisconnectFromProviderEvent event) {
        disconnectProvider(event.mName);
    }

    @SuppressWarnings("unused")
    public void onEvent(DisableEventsEvent event) {
        RsEventBus.unregister(this);
    }

    private void changePlayingProvider(ComponentName providerName) {
        Log.d(TAG, "changePlayingProvider: providerName=" + (providerName != null ? providerName : "null"));
        if (providerName == null) return;

        if (isPlayingProvider(providerName)) return;

        if (mPlayingProvider != null && mPlayingProvider.isPlaying()) mPlayingProvider.forcePause();
        mPlayingProvider = mManger.getProvider(providerName);
        RsEventBus.postSticky(new NowPlayingProviderChangedEvent(mPlayingProvider.getView()));
        if (mBrowsedProvider == null) {
            changeBrowsedProvider(providerName, false);
        }
    }

    public void changeBrowsedProvider(ComponentName providerName, boolean showPlayer) {
        Log.d(TAG, "changeBrowsedProvider: providerName=" + (providerName != null ? providerName : "null") + ", showPlayer=" + showPlayer);
        disconnectBrowsedProvider(false);

        mBrowsedProvider = mManger.getProvider(providerName);
        RsEventBus.postSticky(new CurrentlyBrowsedProviderChanged(mBrowsedProvider.getView()));
        if (!mBrowsedProvider.isConnected()) {
            mBrowsedProvider.connect(showPlayer);
        }

        if (mPlayingProvider == null) {
            changePlayingProvider(providerName);
        }
    }

    private void disconnectBrowsedProvider(boolean unconditionalDisconnect) {
        Log.d(TAG, "disconnectBrowsedProvider: unconditionalDisconnect=" + unconditionalDisconnect);
        if (mBrowsedProvider == null) return;
        if (!mBrowsedProvider.isConnected()) return;

        // don't disconnect when browserProvider is equal playingProvider
        if (unconditionalDisconnect || mPlayingProvider != null && !mBrowsedProvider.isNameEqual(mPlayingProvider.getName())) {
            Log.d(TAG, "disconnectBrowsedProvider");
            mBrowsedProvider.disconnect();
        }
    }

    private void disconnectPlayingProvider() {
        Log.d(TAG, "disconnectPlayingProvider");
        if (mPlayingProvider == null) return;
        if (!mPlayingProvider.isConnected()) return;

        mPlayingProvider.disconnect();
    }

    public void disconnectProvider(ComponentName providerName) {
        Log.d(TAG, "disconnectProvider");
        // check if it's playing provider
        if (mPlayingProvider != null && providerName.equals(mPlayingProvider.getName())) {
            if (mPlayingProvider.isPlaying()) {
                mPlayingProvider.forcePause();
            }
            mPlayingProvider.disconnect();
            mPlayingProvider = null;
            RsEventBus.postSticky(new NowPlayingProviderChangedEvent(null));
        }
        // check if it's browsing provider
        if (mBrowsedProvider != null && providerName.equals(mBrowsedProvider.getName())) {
            mBrowsedProvider.disconnect();
            mBrowsedProvider = null;
            RsEventBus.post(new ProviderBrowseCancelEvent());
        }
    }

    private void tryConnectIfDisconnected(Provider provider) {
        Log.d(TAG, "tryConnectIfDisconnected: provider=" + (provider != null ? provider.getName() : "null"));
        if (provider == null) {
            Log.w(TAG, "Cannot reconnect, current provider is null");
            return;
        }
        if (!provider.isConnected()) {
            provider.connect(false);
        }
    }

    public void tryConnectIfDisconnected() {
        Log.d(TAG, "tryConnectIfDisconnected");
        tryConnectIfDisconnected(mPlayingProvider);
        tryConnectIfDisconnected(mBrowsedProvider);
    }

    public boolean isDisconnectedProvider(){
        if(mPlayingProvider != null){
            if(!mPlayingProvider.isConnected()){
                return true;
            }
        }
        if(mBrowsedProvider != null){
            if(!mBrowsedProvider.isConnected()){
                return true;
            }
        }
        return false;
    }

    public void changeModePlayer(boolean playerModeOnline) {
        Log.d(TAG, "changeModePlayer: mLastPlayerModeOnline=" + mLastPlayerModeOnline + " playerModeOnline=" + playerModeOnline);
        mManger.changeModePlayer(playerModeOnline);

        String playingProviderId = mPlayingProvider != null ? mPlayingProvider.getView().getId() : null;
        String browsingProviderId = mBrowsedProvider != null ? mBrowsedProvider.getView().getId() : null;
        Log.d(TAG, "Actual providers: Playing=" + playingProviderId + ", Browsing=" + browsingProviderId);

        // check only situation online -> offline
        // all providers can play in situation offline -> online
        if(mLastPlayerModeOnline == null || mLastPlayerModeOnline == playerModeOnline || playerModeOnline){
            mLastPlayerModeOnline = playerModeOnline;
            return;
        }else{
            mLastPlayerModeOnline = playerModeOnline;
        }

        if (mPlayingProvider != null) {
            if (mPlayingProvider.isPlaying()) {
                // pause music if mode is change
                mPlayingProvider.forcePause();
            }
            if (!mPlayingProvider.canPlayOffline()) {
                // cancel playing if provider can't play offline
                disconnectProvider(mPlayingProvider.getName());
            }
        }

        if (mBrowsedProvider != null) {
            // playing provider is browsing provider
            if (playingProviderId != null && browsingProviderId != null &&
                    playingProviderId.equals(browsingProviderId)) {
                if (!mBrowsedProvider.canPlayOffline()) {
                    // cancel browsing if playing browser is browsing browser
                    // and can't play offline
                    disconnectProvider(mBrowsedProvider.getName());
                }
            } else {
                if (!mBrowsedProvider.canPlayOffline()) {
                    // cancel browsing when player isn't offline
                    disconnectProvider(mBrowsedProvider.getName());
                } else if (mPlayingProvider == null) {
                    // if we cancel playing and provider browser can play offline
                    // set it as playing provider
                    changePlayingProvider(mBrowsedProvider.getName());
                }
            }
        }
    }
}
