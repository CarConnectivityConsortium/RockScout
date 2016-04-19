/* Copyright 2014 RealVNC ltd.
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

import com.mirrorlink.android.commonapi.IConnectionListener;

/**
 * Provides the interface related to 4.4 Connection Information.
 * <br>
 * The callbacks are defined in {@link IConnectionListener}.
 *
 * <br>
 * <i>Module reference: 0x03</i>
 * <br>
 * <i>Server requirement: Mandatory</i>
 */
interface IConnectionManager {

    /**
     * 4.4.1 Indicates whether a MirrorLink session is currently established.
     *
     * <br>
     * <i>Function reference 0x0301.</i>
     * <br>
     * A MirrorLink is considered established if a ClientProfile has been
     * set on the MirrorLink Server for the current tethering session.
     * <br>
     * The application MUST use this call and its equivalent callback {@link
     * IConnectionListener#onMirrorLinkSessionChanged} to determine whether a
     * MirrorLink session is established. MirrorLink applications SHOULD use
     * other Common API modules only while a MirrorLink Session is running.
     * MirrorLink Servers MUST have the Common API modules available at all
     * times.
     */
    boolean isMirrorLinkSessionEstablished();

    /**
     * 4.4.3 Established Audio Connections.
     *
     * <br>
     * <i>Function reference 0x0303.</i>
     * <br>
     * Established Audio connections within MirrorLink Session
     *
     * @return Bundle containing the status of the audio connections available. The details of the
     * fields available are found in {@link Defs.AudioConnections}.
     */
    Bundle getAudioConnections();

    /**
     * 4.4.5 Established Remote Display Connection.
     *
     * <br>
     * <i>Function reference 0x0305.</i>
     * <br>
     * Established remote display connection within MirrorLink Session.
     *
     * @return Value containing the status of the remote display connections available. The values
     * are defined in {@link Defs.RemoteDisplayConnection}.
     */
    int getRemoteDisplayConnections();

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
