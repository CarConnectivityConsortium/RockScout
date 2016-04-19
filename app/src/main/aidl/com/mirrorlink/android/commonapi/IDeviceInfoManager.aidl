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

import com.mirrorlink.android.commonapi.IDeviceInfoListener;

/**
 * Provides the interface related to 4.2 MirrorLink Device Info.
 * <br>
 * The callbacks are defined in {@link IDeviceInfoListener}.
 *
 * <br>
 * <i>Module reference: 0x01</i>
 * <br>
 * <i>Server requirement: Mandatory</i>
 */
interface IDeviceInfoManager {

    /**
     * 4.2.1 MirrorLink Session Major Version.
     *
     * <br>
     * <i>Function reference 0x0101.</i>
     * <br>
     * Available MirrorLink Version for the established connection, as agreed between the MirrorLink
     * Server and Client. Information MUST be available as soon as the MirrorLink session is
     * connected
     *
     * @return  MirrorLink Session major version
     *          or 1 if version information is not available.
     */
    int getMirrorLinkSessionVersionMajor();

    /**
     * 4.2.1 MirrorLink Session Minor Version.
     *
     * <br>
     * <i>Function reference 0x0101.</i>
     * <br>
     * Available MirrorLink Version for the established connection, as agreed between the MirrorLink
     * Server and Client. Information MUST be available as soon as the MirrorLink session is
     * connected
     *
     * @return  MirrorLink Session minor version
     *          or 0 if version information is not available.
     */
    int getMirrorLinkSessionVersionMinor();

    /**
     * 4.2.2 MirrorLink Client Manufacturer and Model Information.
     *
     * <br>
     * <i>Function reference 0x0103.</i>
     * <br>
     * Provided MirrorLink client manufacturer and model information, as received through the UPnP
     * Client Profile Service; any later change to the provided information MUST be notified via the
     * callback function.
     *
     * @return Bundle containg the client information or null if no client is connected. The bundle
     * will contain the values defined in {@link Defs.ClientInformation}.
     */
    Bundle getMirrorLinkClientInformation();

    /**
     * 4.2.5 Server Device Virtual Keyboard Support.
     *
     * <br>
     * <i>Function reference 0x0105.</i>
     * <br>
     * Provides information about the available virtual keyboard from the MirrorLink Server,
     * which can be used from application, during a MirrorLink session. Handling of the virtual
     * keyboard is following regular Android means.
     *
     * @return Bundle containg the virtual keyboard support. It will contain the values defined in
     * {@link Defs.VirtualKeyboardSupport}.
     */
    Bundle getServerVirtualKeyboardSupport();

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
