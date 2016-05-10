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
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.util.Log;
import android.util.TypedValue;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.DisableEventsEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderDiscoveredEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderDiscoveryFinished;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderToDownloadDiscoveredEvent;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;

import java.util.*;

/**
 * Created by belickim on 16/04/15.
 */
public final class ProvidersManager {
    public static final String TAG = ProvidersManager.class.getSimpleName();
    public static final String ML_OFFLINE_PLAYBACK = "com.mirrorlink.android.rockscout.allow-offline-access";
    public static final int DEFAULT_COLOR = 0x041424; /* TODO: fetch this from resources */
    public static final int TIMER_PERIOD = 120;

    private final Context mContext;
    private final PackageManager mManager;

    private final HashMap<ComponentName, ProviderRecord> mRecords;
    private final HashMap<ComponentName, Boolean> mConnectedProviders;

    private HashMap<ComponentName, ProviderDiscoveryData> mProvidersTestStatus;
    private Timer mDiscoveryCompletedCheckTimer;

    public Context getContext() {
        return mContext;
    }

    public ProvidersManager(Context context, PackageManager manager) {
        mContext = context;
        mManager = manager;
        mRecords = new HashMap<>();
        mConnectedProviders = new HashMap<>();
        RsEventBus.register(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(DisableEventsEvent event) {
        RsEventBus.unregister(this);
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderDiscoveredEvent event) {
        if (mProvidersTestStatus != null) {
            final ComponentName name = event.provider.getUniqueName();
            mProvidersTestStatus.put(name, new ProviderDiscoveryData(true));
        }
    }

    private void checkIfTestConnectionsCompleted() {
        if (mProvidersTestStatus == null) return;

        for (HashMap.Entry<ComponentName, ProviderDiscoveryData> entry
                : mProvidersTestStatus.entrySet()) {
            final ProviderDiscoveryData value = entry.getValue();
            if (value.discoveryFinished == false) {
                final Date now = new Date();
                final long seconds = (now.getTime() - value.creationTime.getTime()) / 1000;
                /* treat this as undiscovered only if discovery started less than 5 seconds ago */
                if (seconds < 5) {
                    return;
                }
            }
        }
        final ProviderDiscoveryFinished finishedEvent
                = new ProviderDiscoveryFinished(mProvidersTestStatus.size());
        RsEventBus.postSticky(finishedEvent);
        mProvidersTestStatus = null;

        if (mDiscoveryCompletedCheckTimer != null) {
            mDiscoveryCompletedCheckTimer.cancel();
            mDiscoveryCompletedCheckTimer = null;
        }
    }

    private List<ResolveInfo> getMediaBrowserPackages() {
        final Intent intent = new Intent(MediaBrowserService.SERVICE_INTERFACE);
        return mManager.queryIntentServices(intent, 0);
    }

    private boolean checkIfCanPlayOffline(ResolveInfo packageInfo) {
        ApplicationInfo appInfo = null;
        try {
            appInfo = mManager.getApplicationInfo(packageInfo.serviceInfo.packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Something went wrong: ", e);
            return false;
        }
        if (appInfo.metaData != null) {
            return appInfo.metaData.getBoolean(ML_OFFLINE_PLAYBACK, false);
        } else {
            return false;
        }
    }

    private Provider addNewProvider(ResolveInfo packageInfo) {
        final boolean canPlayOffline = checkIfCanPlayOffline(packageInfo);
        final Provider provider = new Provider(this, packageInfo, canPlayOffline);
        final ProviderView view = createView(provider, packageInfo);
        final ComponentName name = provider.getName();

        provider.testConnection();
        mProvidersTestStatus.put(name, new ProviderDiscoveryData(false));
        mRecords.put(provider.getName(), new ProviderRecord(provider, view));
        return provider;
    }

    private void startDiscoveryFinishedCheck() {
        mDiscoveryCompletedCheckTimer = new Timer();
        mDiscoveryCompletedCheckTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkIfTestConnectionsCompleted();
            }
        }, 0, TIMER_PERIOD);
    }

    public void findProviders() {
        mRecords.clear();
        mConnectedProviders.clear();
        mProvidersTestStatus = new HashMap<>();
        final List<ResolveInfo> packages = getMediaBrowserPackages();
        for (ResolveInfo packageInfo : packages) {
            final Provider provider = addNewProvider(packageInfo);
            Log.d(TAG, "Added provider : " + provider.getName().toString());
        }
        startDiscoveryFinishedCheck();
    }

    private ComponentName namePackage(ResolveInfo packageInfo) {
        final ServiceInfo serviceInfo = packageInfo.serviceInfo;
        return new ComponentName
                (serviceInfo.applicationInfo.packageName, serviceInfo.name);
    }

    private Set<ComponentName> getNames(List<ResolveInfo> packages) {
        Set<ComponentName> names = new HashSet<>();
        for (ResolveInfo packageInfo : packages) {
            names.add(namePackage(packageInfo));
        }
        return names;
    }

    private void removeObsoleteProviders(List<ResolveInfo> currentPackages) {
        Set<ComponentName> currentNames = getNames(currentPackages);
        for (ComponentName name : mRecords.keySet()) {
            if (currentNames.contains(name) == false) {
                mRecords.remove(name);
                mConnectedProviders.remove(name);
                /* TODO: send an event here to notify launcher view */
            }
        }
    }

    private int addNewProviders(List<ResolveInfo> packages) {
        int added = 0;
        Set<ComponentName> oldNames = mRecords.keySet();
        for (ResolveInfo packageInfo : packages) {
            final ComponentName name = namePackage(packageInfo);
            if (oldNames.contains(name) == false) {
                addNewProvider(packageInfo);
                added += 1;
            }
        }
        return added;
    }

    public void refreshProviders() {
        mProvidersTestStatus = new HashMap<>();
        final List<ResolveInfo> packages = getMediaBrowserPackages();
        removeObsoleteProviders(packages);
        final int addedCount = addNewProviders(packages);
        if (addedCount > 0) {
            startDiscoveryFinishedCheck();
        }
    }

    private ProviderRecord getProviderRecord(ComponentName name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null.");
        }
        if (mRecords.containsKey(name) == false) {
            throw new IllegalArgumentException("Received name of unknown component.");
        }
        return mRecords.get(name);
    }

    public ProviderView getProviderView(ComponentName name) {
        return getProviderRecord(name).immutableView;
    }

    public Provider getProvider(ComponentName name) {
        return getProviderRecord(name).provider;
    }

    private ProviderView createView(Provider provider, ResolveInfo packageInfo) {
        final String label = packageInfo.loadLabel(mManager).toString();
        final Drawable icon = packageInfo.loadIcon(mManager);
        Integer colorPrimaryDark = null;
        Integer colorAccent = null;
        Drawable notificationDrawable = null;
        try {
            final String name = packageInfo.serviceInfo.packageName;
            ApplicationInfo appInfo
                    = mManager.getApplicationInfo(name, PackageManager.GET_META_DATA);
            Resources resources = mManager.getResourcesForApplication(name);
            Resources.Theme theme = resources.newTheme();
            int themeId = resolveAndroidAutoThemeId(appInfo);
            if (themeId > 0) {
                theme.applyStyle(themeId, true);
            }
            colorPrimaryDark = resolveThemeColor(android.R.attr.colorPrimaryDark, theme);
            colorAccent = resolveThemeColor(android.R.attr.colorAccent, theme);
            int notificationDrawableId = resolveAndroidAutoNotificationIcon(appInfo);
            if (notificationDrawableId > 0) {
                notificationDrawable = resources.getDrawable(notificationDrawableId, theme);
            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error reading app theme data for service: " + packageInfo.serviceInfo.packageName, e);
        }
        return new ProviderView(provider, label, icon, colorPrimaryDark, colorAccent, notificationDrawable);
    }

    private static final String CAR_THEME_KEY = "com.google.android.gms.car.application.theme";

    private int resolveAndroidAutoThemeId(ApplicationInfo appInfo) {
        if (appInfo == null) return 0;
        if (appInfo.metaData == null) return 0;

        final Bundle metaData = appInfo.metaData;
        int result = metaData.getInt(CAR_THEME_KEY);
        if (result == 0) {
            result = appInfo.theme;
        }
        return result;
    }

    private static final String CAR_NOTIFICATION_SMALL_ICON = "com.google.android.gms.car.notification.SmallIcon";

    private int resolveAndroidAutoNotificationIcon(ApplicationInfo appInfo) {
        if (appInfo == null) return 0;
        if (appInfo.metaData == null) return 0;

        final Bundle metaData = appInfo.metaData;
        return metaData.getInt(CAR_NOTIFICATION_SMALL_ICON);
    }

    private Integer resolveThemeColor(int colorId, Resources.Theme theme) {
        final TypedValue value = new TypedValue();
        boolean result = theme.resolveAttribute(colorId, value, true);
        if (result) {
            return value.data;
        } else {
            return DEFAULT_COLOR;
        }
    }

    public boolean isConnected(ComponentName name) {
        return getProvider(name).isConnected();
    }

    public void addConnectedProvider(ComponentName componentName, boolean isPlaying) {
        mConnectedProviders.put(componentName, isPlaying);
    }

    public void changeModePlayer(boolean mPlayerModeOnline) {
        int count = 0;
        for (HashMap.Entry<ComponentName, Boolean> entry : mConnectedProviders.entrySet()
                ) {
            Provider provider = getProvider(entry.getKey());
            boolean isPlaying = entry.getValue();

            ProviderView view = getProviderView(entry.getKey());
            if (!mPlayerModeOnline && provider.canPlayOffline() ||
                    mPlayerModeOnline) {
                count++;
                RsEventBus.post(new ProviderDiscoveredEvent(view, isPlaying));
            } else {
                ProviderToDownloadView providerToDownloadView = new ProviderToDownloadView(view.getDisplayInfo().label, view.getDisplayInfo().icon);
                RsEventBus.post(new ProviderToDownloadDiscoveredEvent(providerToDownloadView));
            }

            entry.setValue(false);
        }

        //download compatible apps from server
        if (count <= 0) {
            new ProviderToDownloadParser().download();
        }
    }

    private class ProviderRecord {
        final public Provider provider;
        final public ProviderView immutableView;

        public ProviderRecord(Provider provider, ProviderView immutableView) {
            this.provider = provider;
            this.immutableView = immutableView;
        }
    }

    private class ProviderDiscoveryData {
        final public boolean discoveryFinished;
        final public Date creationTime;

        ProviderDiscoveryData(boolean done) {
            discoveryFinished = done;
            creationTime = new Date();
        }
    }
}
