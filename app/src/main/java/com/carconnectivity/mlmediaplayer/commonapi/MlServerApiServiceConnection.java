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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.util.Log;
import com.mirrorlink.android.commonapi.Defs;
import com.mirrorlink.android.commonapi.ICommonAPIService;

import java.util.List;

public class MlServerApiServiceConnection implements ServiceConnection {
    private final static String LOG_TAG = MlServerApiServiceConnection.class.getCanonicalName();
    private ServiceConnectedCallback onServiceConnectedCallback = null;
    private ServiceDisconnectedCallback onServiceDisconnectedCallback = null;

    private Application applicationContext;

    public MlServerApiServiceConnection(Application arg, ServiceConnectedCallback con, ServiceDisconnectedCallback dcon) {
        this.applicationContext = arg;
        this.onServiceConnectedCallback = con;
        this.onServiceDisconnectedCallback = dcon;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(LOG_TAG, "onServiceConnected Called");
        ICommonAPIService s = ICommonAPIService.Stub.asInterface(service);
        if (onServiceConnectedCallback != null) {
            onServiceConnectedCallback.connected(s);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(LOG_TAG, "onServiceDisconnected Called");
        if (onServiceDisconnectedCallback != null) {
            onServiceDisconnectedCallback.disconnected();
        }
    }

    public void disconnectService() {
        Log.d(LOG_TAG, "disconnectService Called");
        this.applicationContext.unbindService(this);
        if (onServiceDisconnectedCallback != null) {
            onServiceDisconnectedCallback.disconnected();
        }

    }

    public boolean connectService() {
        Log.d(LOG_TAG, "connectService Called");

        // new standard of binding Intets - Android 5.0
        Intent implicitIntent = new Intent(Defs.Intents.BIND_MIRRORLINK_API);
        Intent bindIntent = createExplicitFromImplicitIntent(this.applicationContext, implicitIntent);

        // verify if MirrrorLink is available - bindIntent != NULL
        if (bindIntent != null) {
            this.applicationContext.bindService(bindIntent, this, Context.BIND_AUTO_CREATE);
            // Is supported
            MirrorLinkConnectionManager.mIsMirrorLinkSupported = true;
            return true;
        } else {
            // TODO : Unsupported communicate
            return false;
        }

    }


    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

}
