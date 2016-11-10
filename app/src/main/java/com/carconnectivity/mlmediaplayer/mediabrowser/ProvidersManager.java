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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.util.Log;
import android.util.TypedValue;

import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.DisableEventsEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderDiscoveredEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderDiscoveryFinished;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderInactiveDiscoveredEvent;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by belickim on 16/04/15.
 */
public final class ProvidersManager {

    public static final String TAG = ProvidersManager.class.getSimpleName();
    public static final String ML_OFFLINE_PLAYBACK = "com.mirrorlink.android.rockscout.allow-offline-access";
    public static final int TIMER_PERIOD = 120;
    private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD = "com.google.market";
    private static final String GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW = "com.android.vending";

    private final Context mContext;
    private final PackageManager mManager;

    private final HashMap<ComponentName, ProviderRecord> mRecords;
    private final HashMap<ComponentName, Boolean> mConnectedProviders;

    private HashMap<ComponentName, ProviderDiscoveryData> mProvidersTestStatus;
    private Timer mDiscoveryCompletedCheckTimer;
    private Set<ComponentName> mMediaBrowserPackages;

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
        RsEventBus.post(finishedEvent);
        mProvidersTestStatus = null;

        if (mDiscoveryCompletedCheckTimer != null) {
            mDiscoveryCompletedCheckTimer.cancel();
            mDiscoveryCompletedCheckTimer = null;
        }
    }

    private List<ResolveInfo> getMediaBrowserPackages() {
        final Intent intent = new Intent(MediaBrowserService.SERVICE_INTERFACE);
        List<ResolveInfo> resolveInfos = mManager.queryIntentServices(intent, 0);
        mMediaBrowserPackages = getNames(resolveInfos);
        return resolveInfos;
    }

    private boolean checkIfCanPlayOffline(ResolveInfo packageInfo) {
        ApplicationInfo appInfo;
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
        final ProviderViewActive view = createView(provider, packageInfo);
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
        boolean removed = false;
        Set<ComponentName> currentNames = getNames(currentPackages);

        for (Iterator<Map.Entry<ComponentName, ProviderRecord>> it = mRecords.entrySet().iterator(); it.hasNext(); ) {
            ComponentName name = it.next().getKey();
            if (currentNames.contains(name) == false) {
                it.remove();
                mConnectedProviders.remove(name);
                removed = true;
            }
        }

        if (removed) {
            final ProviderDiscoveryFinished finishedEvent
                    = new ProviderDiscoveryFinished(mRecords.size());
            RsEventBus.postSticky(finishedEvent);
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

    public ProviderViewActive getProviderView(ComponentName name) {
        return getProviderRecord(name).immutableView;
    }

    public Provider getProvider(ComponentName name) {
        return getProviderRecord(name).provider;
    }

    private ProviderViewActive createView(Provider provider, ResolveInfo packageInfo) {
        final String label = packageInfo.loadLabel(mManager).toString();
        final Drawable icon = packageInfo.loadIcon(mManager);
        Integer colorPrimaryDark = null;
        Integer colorAccent = null;
        Drawable notificationDrawable = null;
        String name = "";
        try {
            name = packageInfo.serviceInfo.packageName;
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
        return new ProviderViewActive(provider, label, name, icon, colorPrimaryDark, colorAccent, notificationDrawable);
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
            return mContext.getResources().getColor(R.color.provider_default_theme_color);
        }
    }

    public boolean isConnected(ComponentName name) {
        return getProvider(name).isConnected();
    }

    public void addTestedProvider(ComponentName componentName, boolean connected, boolean isPlaying) {
        if (mProvidersTestStatus == null) return;
        if (connected) {
            mConnectedProviders.put(componentName, isPlaying);
        }
        mProvidersTestStatus.put(componentName, new ProviderDiscoveryData(true));
    }

    public void changeModePlayer(boolean mPlayerModeOnline) {
        for (HashMap.Entry<ComponentName, Boolean> entry : mConnectedProviders.entrySet()
                ) {
            Provider provider = getProvider(entry.getKey());
            boolean isPlaying = entry.getValue();

            ProviderViewActive viewOnline = getProviderView(entry.getKey());
            if (!mPlayerModeOnline && provider.canPlayOffline() ||
                    mPlayerModeOnline) {
                RsEventBus.post(new ProviderDiscoveredEvent(viewOnline, isPlaying));
            } else {
                ProviderViewInactive providerViewInactive = new ProviderViewInactive(viewOnline.getLabel(), viewOnline.getId(),
                        viewOnline.getIconDrawable());
                RsEventBus.post(new ProviderInactiveDiscoveredEvent(providerViewInactive));
            }

            entry.setValue(false);
        }
        if (!mPlayerModeOnline && checkIfGooglePlayIsInstalled()) {
            //download compatible apps from server
            new Thread(new Runnable() {
                @Override
                public void run() {
                    new ProviderToDownloadParser(mMediaBrowserPackages).download();
                }
            }).start();
        }
    }

    public boolean isRecordsContainsProvider(ComponentName componentName) {
        return mRecords.containsKey(componentName);
    }

    private boolean checkIfGooglePlayIsInstalled() {
        try {
            mManager.getPackageInfo(GOOGLE_PLAY_STORE_PACKAGE_NAME_OLD, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        try {
            mManager.getPackageInfo(GOOGLE_PLAY_STORE_PACKAGE_NAME_NEW, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    private class ProviderRecord {
        final public Provider provider;
        final public ProviderViewActive immutableView;

        public ProviderRecord(Provider provider, ProviderViewActive immutableView) {
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
