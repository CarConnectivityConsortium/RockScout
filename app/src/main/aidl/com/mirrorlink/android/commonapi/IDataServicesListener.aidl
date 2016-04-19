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

/**
 * Provides the interface related to 4.11 Data Services - Callbacks.
 * <br>
 * This is the interface for callbacks coming from {@link IDataServicesManager}.
 */
oneway interface IDataServicesListener {

    /**
     * 4.11.2 Available Services Callback.
     *
     * <br>
     * <i>Function reference 0x0A02.</i>
     * <br>
     * Change in available services. Callback must be called, when CDB connection is established.
     *
     * @param  services List of provided services.  The list contains Bundles with the fields as
     * defined in {@link Defs.ServiceInformation}.
     */
    void onAvailableServicesChanged(in List<Bundle> services);

    /**
     * 4.11.4 Register to a Service Callback.
     *
     * <br>
     * <i>Function reference 0x0A04.</i>
     * <br>
     * Registration completed.
     *
     * @param serviceId Service identifier.
     * @param success Flag, to indicate whether the action is successful.
     */
    void onRegisterForService(in int serviceId, in boolean success);

    /**
     * 4.11.7 Subscribe to an Object Callback.
     *
     * <br>
     * <i>Function reference 0x0A07.</i>
     * <br>
     * Subscription complete.
     * <br>
     * If the subcription was successful then any update to the value of the data will be provided
     * via {@link #onGetDataObjectResponse}.
     *
     * @param serviceId Service identifier.
     * @param objectId Hash value of the object.
     * @param success Flag, to indicate whether the action is successful.
     * @param subscriptionType The subscription type. Will have one of the values defined in {@link
     * Defs.SubscriptionType}.
     * @param interval Regular time interval in ms, in which updates are sent. MUST be 0 for
     * subscription types {@link Defs.SubscriptionType#ON_CHANGE} and {@link
     * Defs.SubscriptionType#AUTOMATIC}.
     */
    void onSubscribeResponse(in int serviceId, in int objectId, in boolean success,
            in int subscriptionType, in int interval);

    /**
     * 4.11.10 Set an Object Callback.
     *
     * <br>
     * <i>Function reference 0x0A0A.</i>
     * <br>
     * Object set.
     *
     * @param serviceId Service identifier
     * @param objectId Hash value of the object
     * @param success Flag to indicate whether the action is successful.
     */
    void onSetDataObjectResponse(in int serviceId, in int objectId, boolean success);

    /**
     * 4.11.11 Get an Object Callback.
     *
     * <br>
     * <i>Function reference 0x0A0C.</i>
     * <br>
     * Object received, packaged as a Bundle as described in {@link Defs.DataObjectKeys}.
     *
     * @param serviceId Service identifier
     * @param objectId Hash value of the object
     * @param success Flag to indicate whether the action is successful.
     * @param object Bundle containing the object payload.
     */
    void onGetDataObjectResponse(in int serviceId, in int objectId, boolean success, in Bundle object);

}


