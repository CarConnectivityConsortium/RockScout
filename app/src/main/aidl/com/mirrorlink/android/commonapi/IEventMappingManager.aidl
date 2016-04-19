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

import com.mirrorlink.android.commonapi.IEventMappingListener;

/**
 * Provides the interface related to 4.6 Event Features.
 * <br>
 * The callbacks are defined in {@link IEventMappingListener}.
 *
 * <br>
 * <i>Module reference: 0x05</i>
 * <br>
 * <i>Server requirement: Mandatory</i>
 */
interface IEventMappingManager {

    /**
     * 4.6.1 Event Configuration.
     *
     * <br>
     * <i>Function reference 0x0501.</i>
     * <br>
     * Access information on the event properties of the MirrorLink connection, i.e. the event
     * properties, which are supported from both, the MirrorLink Server and MirrorLink Client;
     * details on the event configuration are specified in the VNC specification.
     * <br>
     * Requires established VNC connection; any later change to the provided information MUST be
     * notified via the callback function {@link IEventMappingListener#onEventConfigurationChanged}.
     *
     * @return  The event configuration of the connection as specified in MirrorLink VNC
     * specification. The fields available in the return type are defined in {@link
     * Defs.EventConfiguration}.
     */
    Bundle getEventConfiguration();

    /**
     * 4.6.3 Get Event Mapping.
     * <br>
     * Function reference 0x0503 and 0x0504.
     * <br>
     * Mapping MirrorLink Client events to local MirrorLink Server events; this API call gives
     * access to the internal mapping in the MirrorLink Server.
     * <br>
     * Requires established VNC connection; any later change to the provided information MUST be
     * notified via the callback function {@link IEventMappingListener#onEventMappingChanged}.
     *
     * @return  The key mapping information about remote events and local events. This is a list of
     * Bundles that have their fields defined in {@link Defs.EventMapping}.
     */
    List<Bundle> getEventMappings();

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
