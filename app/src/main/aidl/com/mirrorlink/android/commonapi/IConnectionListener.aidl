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
 * Provides the interface related 4.4 Connection Information - Callbacks.
 * <br>
 * This is the interface for callbacks coming from {@link IConnectionManager}.
 */
oneway interface IConnectionListener {

    /**
     * 4.4.2 Established MirrorLink Session Callback.
     *
     * <br>
     * <i>Function reference 0x0302.</i>
     * <br>
     * Indicate that the MirrorLink Session status has changed.
     * <br>
     * The application MUST use this call and its equivalent callback {@link
     * IConnectionManager#isMirrorLinkSessionEstablished} to determine whether a
     * MirrorLink session is established. MirrorLink applications SHOULD use
     * other Common API modules only while a MirrorLink Session is running.
     * MirrorLink Servers MUST have the Common API modules available at all
     * times.
     *
     * @param mirrolinkSessionIsEstablished the new status of the MirrorLink session.
     */
    void onMirrorLinkSessionChanged(in boolean mirrolinkSessionIsEstablished);

    /**
     * 4.4.4 Established Audio Connections Callback.
     *
     * <br>
     * <i>Function reference 0x0304.</i>
     * <br>
     * Indicate that the audio connections changed.
     *
     * @param audioConnections Bundle containing the status of the audio connections available. The
     * details of the fields available are found in {@link Defs.AudioConnections}.
     */
    void onAudioConnectionsChanged(in Bundle audioConnections);

    /**
     * 4.4.6 Established Remote Display Connection Callback.
     *
     * <br>
     * <i>Function reference 0x0306.</i>
     * <br>
     * Indicate that the remote display connections changed.
     *
     * @param remoteDisplayConnection integer indicating the status of the remote display connections
     * available. The values are defined in {@link Defs.RemoteDisplayConnection}.
     */
    void onRemoteDisplayConnectionChanged(in int remoteDisplayConnection);

}
