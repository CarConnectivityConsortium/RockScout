/* Copyright 2013-2014 RealVNC ltd.
 * Portions Copyright 2011-2014 Car Connectivity Consortium LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mirrorlink.android.commonapi;

import com.mirrorlink.android.commonapi.ICertificationManager;
import com.mirrorlink.android.commonapi.IConnectionManager;
import com.mirrorlink.android.commonapi.IContextManager;
import com.mirrorlink.android.commonapi.IDataServicesManager;
import com.mirrorlink.android.commonapi.IDeviceInfoManager;
import com.mirrorlink.android.commonapi.IDeviceStatusManager;
import com.mirrorlink.android.commonapi.IDisplayManager;
import com.mirrorlink.android.commonapi.IEventMappingManager;
import com.mirrorlink.android.commonapi.INotificationManager;

import com.mirrorlink.android.commonapi.ICertificationListener;
import com.mirrorlink.android.commonapi.IConnectionListener;
import com.mirrorlink.android.commonapi.IContextListener;
import com.mirrorlink.android.commonapi.IDataServicesListener;
import com.mirrorlink.android.commonapi.IDeviceInfoListener;
import com.mirrorlink.android.commonapi.IDeviceStatusListener;
import com.mirrorlink.android.commonapi.IDisplayListener;
import com.mirrorlink.android.commonapi.IEventMappingListener;
import com.mirrorlink.android.commonapi.INotificationListener;

/**
 * The main interface through which the MirrorLink Common API
 * services are to be accessed.
 *
 * <br>
 * <i>Module reference: 0xF0</i>
 * <br>
 * <i>Server requirement: Mandatory</i>
 */
interface ICommonAPIService {
    /**
     * Reports the current CommonAPI api level supported by the service,
     * as opposed to the api level specified by this interface.
     *
     * <br>
     * <i>Function reference 0xF001.</i>
     *
     * @return The MirrorLink API level.
     */
    int getCommonAPIServiceApiLevel();

    /**
     * Notifies the MirrorLink Server that an application has started.
     * <br>
     * Applications MUST call this on start-up, whether they are launched by the MirrorLink server,
     * or started otheriwse.
     *
     * @param packageName The name of the package of the application.
     * @param commonApiLevel The level of the MirrorLink Common API implemented by the application.
     */
    void applicationStarted(in String packageName, in int commonApiLevel);

    /**
     * Notifies the MirrorLink Server that an application is stopping.
     * <br>
     * Applications MUST call this on shut-down, whether they are terminated by the MirrorLink server,
     * or stopped otheriwse.
     *
     * @param packageName The name of the package of the application.
     */
    void applicationStopping(in String packageName);

    /**
     * Returns a ICertificationManager for handling certificates.
     *
     * <br>
     * <i>Function reference 0xF002.</i> In the Android Common API there is no method to check if a module
     * is available (by passing in the module reference), but there are get-functions for each
     * module and if null is returned then the module is not available.
     * <br>
     * This service is mandatory so a reference to the manager should be returned.
     *
     * @param packageName The name of the package of the application.
     * @param listener The Listener object for the Manager.
     *
     * @return An instance of {@link ICertificationManager}.
     */
    ICertificationManager getCertificationManager(in String packageName, in ICertificationListener listener);

    /**
     * Returns a IConnectionManager for requesting connection information.
     *
     * <br>
     * <i>Function reference 0xF002.</i> In the Android Common API there is no method to check if a module
     * is available (by passing in the module reference), but there are get-functions for each
     * module and if null is returned then the module is not available.
     * <br>
     * This service is mandatory so a reference to the manager should be returned.
     *
     * @param packageName The name of the package of the application.
     * @param listener The Listener object for the Manager.
     *
     * @return An instance of {@link IConnectionManager}.
     */
    IConnectionManager getConnectionManager(in String packageName, in IConnectionListener listener);

    /**
     * Returns a IContextManagerManager for handling context information.
     *
     * <br>
     * <i>Function reference 0xF002.</i> In the Android Common API there is no method to check if a module
     * is available (by passing in the module reference), but there are get-functions for each
     * module and if null is returned then the module is not available.
     * <br>
     * This service is mandatory so a reference to the manager should be returned.
     *
     * @param packageName The name of the package of the application.
     * @param listener The Listener object for the Manager.
     *
     * @return An instance of {@link IContextManager}.
     */
    IContextManager getContextManager(in String packageName, in IContextListener listener);

    /**
     * Returns a IDataServiceManager for handling Common Data Bus connections.
     *
     * <br>
     * <i>Function reference 0xF002.</i> In the Android Common API there is no method to check if a module
     * is available (by passing in the module reference), but there are get-functions for each
     * module and if null is returned then the module is not available.
     * <br>
     * This service is optional so null may be returned.
     *
     * @param packageName The name of the package of the application.
     * @param listener The Listener object for the Manager.
     *
     * @return An instance of {@link IDataServicesManager}.
     */
    IDataServicesManager getDataServicesManager(in String packageName,
            in IDataServicesListener listener);

    /**
     * Returns a IDeviceInfoManager for handling device information.
     *
     * <br>
     * <i>Function reference 0xF002.</i> In the Android Common API there is no method to check if a module
     * is available (by passing in the module reference), but there are get-functions for each
     * module and if null is returned then the module is not available.
     * <br>
     * This service is mandatory so a reference to the manager should be returned.
     *
     * @param packageName The name of the package of the application.
     * @param listener The Listener object for the Manager.
     *
     * @return An instance of {@link IDeviceInfoManager}.
     */
    IDeviceInfoManager getDeviceInfoManager(in String packageName, in IDeviceInfoListener listener);

    /**
     * Returns a IDeviceStatusManager for handling device status.
     *
     * <br>
     * <i>Function reference 0xF002.</i> In the Android Common API there is no method to check if a module
     * is available (by passing in the module reference), but there are get-functions for each
     * module and if null is returned then the module is not available.
     * <br>
     * This service is mandatory so a reference to the manager should be returned.
     *
     * @param packageName The name of the package of the application.
     * @param listener The Listener object for the Manager.
     *
     * @return An instance of {@link IDeviceStatusManager}.
     */
    IDeviceStatusManager getDeviceStatusManager(in String packageName,
            in IDeviceStatusListener listener);

    /**
     * Returns a IDisplayManager for handling remote displays.
     *
     * <br>
     * <i>Function reference 0xF002.</i> In the Android Common API there is no method to check if a module
     * is available (by passing in the module reference), but there are get-functions for each
     * module and if null is returned then the module is not available.
     * <br>
     * This service is mandatory so a reference to the manager should be returned.
     *
     * @param packageName The name of the package of the application.
     * @param listener The Listener object for the Manager.
     *
     * @return An instance of {@link IDisplayManager}.
     */
    IDisplayManager getDisplayManager(in String packageName, in IDisplayListener listener);

    /**
     * Returns a IEventMappingManager for handling event mapping.
     *
     * <br>
     * <i>Function reference 0xF002.</i> In the Android Common API there is no method to check if a module
     * is available (by passing in the module reference), but there are get-functions for each
     * module and if null is returned then the module is not available.
     * <br>
     * This service is mandatory so a reference to the manager should be returned.
     *
     * @param packageName The name of the package of the application.
     * @param listener The Listener object for the Manager.
     *
     * @return An instance of {@link IEventMappingManager}.
     */
    IEventMappingManager getEventMappingManager(in String packageName,
            in IEventMappingListener listener);

    /**
     * Returns a INotificationManager for handling notifications.
     *
     * <br>
     * <i>Function reference 0xF002.</i> In the Android Common API there is no method to check if a module
     * is available (by passing in the module reference), but there are get-functions for each
     * module and if null is returned then the module is not available.
     * <br>
     * This service is optional so null may be returned.
     *
     * @param packageName The name of the package of the application.
     * @param listener The Listener object for the Manager.
     *
     * @return An instance of {@link INotificationManager}.
     */
    INotificationManager getNotificationManager(in String packageName,
            in INotificationListener listener);
}
