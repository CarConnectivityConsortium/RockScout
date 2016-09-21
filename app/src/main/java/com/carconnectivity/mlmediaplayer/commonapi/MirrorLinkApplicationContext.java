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

import android.app.Application;
import android.os.RemoteException;
import android.util.Log;

import com.carconnectivity.mlmediaplayer.commonapi.events.ConnectionMirrorLinkServiceEvent;
import com.carconnectivity.mlmediaplayer.utils.LogUtils;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;
import com.mirrorlink.android.commonapi.ICommonAPIService;
import com.mirrorlink.android.commonapi.IConnectionListener;
import com.mirrorlink.android.commonapi.IConnectionManager;
import com.mirrorlink.android.commonapi.IContextListener;
import com.mirrorlink.android.commonapi.IContextManager;
import com.mirrorlink.android.commonapi.IDeviceStatusListener;
import com.mirrorlink.android.commonapi.IDeviceStatusManager;

import java.util.ArrayList;
import java.util.List;

public final class MirrorLinkApplicationContext extends Application {
    public static final String TAG = MirrorLinkApplicationContext.class.getSimpleName();

    private static volatile ICommonAPIService mService = null;
    private MlServerApiServiceConnection mlsConnection = null;

    private IDeviceStatusManager mDeviceStatusManager;
    private IConnectionManager mConnectionManager = null;
    private IContextManager mContextManager = null;

    private List<IDeviceStatusListener> mDeviceStatusListeners = new ArrayList<>();
    private List<IConnectionListener> mConnectionApplicationListeners = new ArrayList<>();
    private List<IContextListener> mContextApplicationListeners = new ArrayList<>();

    private List<Object> mDeviceStatusManagerReferenceList = new ArrayList<>();
    private List<Object> mConnectionManagerReferenceList = new ArrayList<>();
    private List<Object> mContextManagerReferenceList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.setupLogcatLogs(getApplicationInfo());
    }

    public ICommonAPIService getService() {
        return mService;
    }

    public IConnectionManager getConnectionManager() {
        return mConnectionManager;
    }

    public IContextManager getContextManager() {
        return mContextManager;
    }

    public IDeviceStatusManager getDeviceStatusManager() {
        return mDeviceStatusManager;
    }

    ServiceConnectedCallback serviceConnectedCallback = new ServiceConnectedCallback() {
        @Override
        public void connected(ICommonAPIService service) {
            mService = service;
            RsEventBus.postSticky(new ConnectionMirrorLinkServiceEvent(true));
            try {
                mService.applicationStarted(getPackageName(), 1);
            } catch (RemoteException e) {
                Log.e(TAG, "Something went wrong: ", e);
            }
        }
    };

    ServiceDisconnectedCallback serviceDisconnectedCallback = new ServiceDisconnectedCallback() {
        @Override
        public void disconnected() {
            mService = null;
            RsEventBus.postSticky(new ConnectionMirrorLinkServiceEvent(false));

            mConnectionManager = null;
            mContextManager = null;
        }
    };

    public boolean connect() {
        Log.v(TAG, "Connect to service");
        mlsConnection = new MlServerApiServiceConnection(this, serviceConnectedCallback, serviceDisconnectedCallback);
        return mlsConnection.connectService();
    }

    public void disconnect() {
        Log.v(TAG, "Disconnect from service");
        if (mlsConnection != null)
            mlsConnection.disconnectService();
        mlsConnection = null;
    }

    public void unregisterDeviceStatusManager(Object obj, IDeviceStatusListener listener) {
        mDeviceStatusManagerReferenceList.remove(obj);

        if (listener != null)
            mDeviceStatusListeners.remove(listener);

        Log.v(TAG, "unregisterDeviceStatus local " + obj.getClass().getName());

        if (mDeviceStatusManagerReferenceList.size() == 0) {
            Log.v(TAG, "unregisterDeviceStatusManager global");
            try {
                mDeviceStatusListeners.clear();
                if (mDeviceStatusManager != null) {
                    mDeviceStatusManager.unregister();
                    mDeviceStatusManager = null;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Something went wrong: ", e);
            }
        }
    }

    public void registerDeviceStatusManager(Object obj, IDeviceStatusListener listener) {
        Log.v(TAG, "registerDeviceStatusManager local " + obj.getClass().getName());

        if (listener != null)
            mDeviceStatusListeners.add(listener);

        if (mDeviceStatusManagerReferenceList.size() == 0) {
            Log.v(TAG, "registerDeviceStatusManager global ");
            try {
                mDeviceStatusManager = mService.getDeviceStatusManager(getPackageName(), listener);
            } catch (Exception e) {
                Log.e(TAG, "Something went wrong: ", e);
            }
        }
        mDeviceStatusManagerReferenceList.add(obj);
    }

    public void unregisterConnectionManager(Object obj, IConnectionListener listener) {
        mConnectionManagerReferenceList.remove(obj);

        if (listener != null)
            mConnectionApplicationListeners.remove(listener);

        Log.v(TAG, "unregisterConnectionManager local " + obj.getClass().getName());

        if (mConnectionManagerReferenceList.size() == 0) {
            Log.v(TAG, "unregisterConnectionManager global");
            try {
                mConnectionApplicationListeners.clear();
                if (mConnectionManager != null) {
                    mConnectionManager.unregister();
                    mConnectionManager = null;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Something went wrong: ", e);
            }
        }
    }

    public void registerConnectionManager(Object obj, IConnectionListener listener) {
        Log.v(TAG, "registerConnectionManager local " + obj.getClass().getName());

        if (listener != null)
            mConnectionApplicationListeners.add(listener);

        if (mConnectionManagerReferenceList.size() == 0) {
            Log.v(TAG, "registerConnectionManager global ");
            try {
                mConnectionManager = mService.getConnectionManager(getPackageName(), listener);
            } catch (Exception e) {
                Log.e(TAG, "Something went wrong: ", e);
            }
        }
        mConnectionManagerReferenceList.add(obj);
    }

    public void unregisterContextManager(Object obj, IContextListener listener) {
        mContextManagerReferenceList.remove(obj);

        Log.v(TAG, "unregisterContextManager local " + obj.getClass().getName());

        if (listener != null)
            mContextApplicationListeners.remove(listener);

        if (mContextManagerReferenceList.size() == 0) {
            Log.v(TAG, "unregisterContextManager global");
            try {
                mContextApplicationListeners.clear();
                if (mContextManager != null) {
                    mContextManager.unregister();
                    mContextManager = null;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Something went wrong: ", e);
            }
        }
    }

    public void registerContextManager(Object obj, IContextListener listener) {
        Log.v(TAG, "registerContextManager local " + obj.getClass().getName());

        if (listener != null)
            mContextApplicationListeners.add(listener);

        if (mContextManagerReferenceList.size() == 0) {
            Log.v(TAG, "registerContextManager global ");
            try {
                mContextManager = mService.getContextManager(getPackageName(), listener);
            } catch (Exception e) {
                Log.e(TAG, "Something went wrong: ", e);
            }
        }
        mContextManagerReferenceList.add(obj);
    }
}
