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

import android.os.Bundle;

import com.mirrorlink.android.commonapi.IDataServicesListener;

/**
 * Provides the interface related to 4.11 Data Services.
 * <br>
 * The callbacks are defined in {@link IDataServicesListener}.
 *
 * <br>
 * <i>Module reference: 0x0A</i>
 * <br>
 * <i>Server requirement: Optional</i>
 */
interface IDataServicesManager {
    /**
     * 4.11.1 Get Available Services.
     *
     * <br>
     * <i>Function reference 0x0A01.</i>
     * <br>
     * Retrieve list of available Services provided from the MirrorLink Client and supported from
     * the MirrorLink Server.
     *
     * @return  List of provided services;
     *          an empty list is returned if the CDB connection has not been established. The list
     *          contains Bundles with the fields as defined in {@link Defs.ServiceInformation}.
     */
    List<Bundle> getAvailableServices();

    /**
     * 4.11.3 Register to a Service.
     *
     * <br>
     * <i>Function reference 0x0A03.</i>
     * <br>
     * Register to an available Service.
     *
     * @param serviceId Service identifier. Can be one of {@link
     * Defs.LocationService#LOCATION_OBJECT_UID}, or {@link Defs.GPSService#NMEA_OBJECT_UID}.
     * @param versionMajor The major version of the service supported by the application.
     * @param versionMinor The minor version of the service supported by the application.
     */
    void registerToService(in int serviceId, in int versionMajor, in int versionMinor);

    /**
     * 4.11.5 Unregister from a Service.
     *
     * <br>
     * <i>Function reference 0x0A05.</i>
     * <br>
     * Unregister from an available Service.
     *
     * @param serviceId Service identifier.
     */
    void unregisterFromService(in int serviceId);

    /**
     * 4.11.6 Subscribe to an Object.
     *
     * <br>
     * <i>Function reference 0x0A06.</i>
     * <br>
     * Subscribe a Service Object.
     *
     * @param serviceId Service identifier.
     * @param objectId Hash value of the object.
     */
    void subscribeObject(in int serviceId, in int objectId);

    /**
     * 4.11.8 Unsubscribe from an Object.
     *
     * <br>
     * <i>Function reference 0x0A08.</i>
     * <br>
     * Unsubscribe from a Service Object.
     *
     * @param serviceId Service identifier.
     * @param objectId Hash value of the object.
     */
    void unsubscribeObject(in int serviceId, in int objectId);

    /**
     * 4.11.9 Set an Object.
     *
     * <br>
     * <i>Function reference 0x0A09.</i>
     * <br>
     * Set a Service Object. Requires established CDB connection and registered service.
     *
     * The Object is packaged as a Bundle as described in {@link Defs.DataObjectKeys}.
     *
     * @param serviceId Service identifier.
     * @param objectId the hash value of the object.
     * @param object Bundle containing the object payload.
     */
    void setObject(in int serviceId, in int objectId, in Bundle object);

    /**
     * 4.11.11 Get an Object.
     *
     * <br>
     * <i>Function reference 0x0A0B.</i>
     * <br>
     * Get a Service Object. Requires established CDB connection and registered service.
     *
     * @param serviceId Service identifier
     * @param objectId the hash value of the object
     */
    void getObject(in int serviceId, in int objectId);

    /**
     * Notifies the Manager that the application is not using it anymore.
     * <br>
     * Applications are required to call this method when they no longer need to use the Manager.
     * Once called, if at a later point they need the Manager again, they can re-request access to
     * it from the {@link ICommonAPIService}.
     * <br>
     * Once unregister is received by the server, the application will not receive any more
     * callbacks from the Manager.
     */
    oneway void unregister();
}
