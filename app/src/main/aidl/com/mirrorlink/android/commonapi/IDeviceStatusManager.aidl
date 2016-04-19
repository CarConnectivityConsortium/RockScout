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

import com.mirrorlink.android.commonapi.IDeviceStatusListener;

/**
 * Provides the interface related to 4.10 Device Status.
 * <br>
 * The callbacks are defined in {@link IDeviceStatusListener}.
 *
 * <br>
 * <i>Module reference: 0x09</i>
 * <br>
 * <i>Server requirement: Mandatory</i>
 */
interface IDeviceStatusManager {

    /**
     * 4.10.1 Drive Mode.
     *
     * <br>
     * <i>Function reference 0x0901.</i>
     * <br>
     * Check the drive mode status on the MirrorLink Server; requires established VNC connection.
     *
     * @return Flag set to true if the Server is in Drive Mode.
     */
    boolean isInDriveMode();

    /**
     * 4.10.3 Night Mode.
     *
     * <br>
     * <i>Function reference 0x0903.</i>
     * <br>
     * Check the night mode status on the MirrorLink Server; requires established VNC connection.
     *
     * @return Flag set to true if the Server is in Night Mode.
     */
    boolean isInNightMode();

    /**
     * 4.10.5 Microphone State.
     *
     * <br>
     * <i>Function reference 0x0905.</i>
     * <br>
     * Check the status of the Microphone from the MirrorLink Client; requires established VNC
     * connection.
     *
     * @return Flag set to true if the mic input is enabled on MirrorLink Client.
     */
    boolean isMicrophoneOn();

    /**
     * 4.10.7 Set Open Microphone.
     *
     * <br>
     * <i>Function reference 0x0907.</i>
     * <br>
     * Open the Microphone on the MirrorLink Client.
     *
     * @param micInput Flag enabling mic input on the MirrorLink Client.
     * @param voiceInput Flag enabling voice input on the MirrorLink Client. The application MUST
     * set the Mic Input flag to true if the Voice input flag is set to true.
     */
    boolean setMicrophoneOpen(in boolean micInput, in boolean voiceInput);

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
