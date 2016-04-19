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

import com.mirrorlink.android.commonapi.IDisplayListener;

/**
 * Provides the interface related to 4.5 Display.
 * <br>
 * The callbacks are defined in {@link IDisplayListener}.
 *
 * <br>
 * <i>Module reference: 0x04</i>
 * <br>
 * <i>Server requirement: Mandatory</i>
 * <br>
 * Note: The setFramebufferOrientationSupport method (function reference 0x0405) is not defined
 * because the MirrorLink Server can find out the orientations supported by the application from the
 * information containted within the manifest of the application.
 */
interface IDisplayManager {

    /**
     * 4.5.1 Display Configuration.
     *
     * <br>
     * <i>Function reference 0x0401.</i>
     * <br>
     * Access information on the display properties of the MirrorLink Session; this information is
     * used by MirrorLink certified applications to adapt its user interface to fulfill driver
     * distraction guidelines, in particular regarding font sizes; Requires an established VNC
     * connection; any later change to the provided information are notified via the callback
     * function {@link IDisplayListener#onDisplayConfigurationChanged}.
     * <br>
     * The provided framebuffer resolutions are modeling the following framebuffer pipeline:
     * <ol>
     *  <li>The applications renders its user interface into a framebuffer available in full to
     *  the application (App Horizontal / Vertical Resolution). </li>
     *  <li>The MirrorLink Server scales that framebuffer to better fit the MirrorLink Cli-
     *  ent’s framebuffer properties (Server Horizontal / Vertical Resolution). </li>
     *  <li>The MirrorLink Server adds pad rows and/or columns to the scaled framebuffer
     *  (Server Pad Rows / Columns). </li>
     *  <li>The MirrorLink Server transmits that framebuffer to the MirrorLink Client. </li>
     *  <li>The MirrorLink Client scales the received framebuffer to fit into its framebuffer
     *  (Client Horizontal / Vertical Resolution); the MirrorLink Client may add pad
     *  rows or columns (but not both) to compensate for differences in the framebuffer
     *  aspect ratio. Those pad rows or columns to not take away any resolution from the
     *  transmitted MirrorLink Server framebuffer.  </li>
     * </ol>
     * <br>
     * All pixel-based resolutions are based on a pixel aspect ratio of 1 (one), i.e. a squared
     * pixel.
     *
     * @return Bundle object containing the display configuration, as defined in {@link
     * Defs.DisplayConfiguration}, of the MirrorLink session.
     */
    Bundle getDisplayConfiguration();

    /**
     * 4.5.3 Client Pixel Format.
     *
     * <br>
     * <i>Function reference 0x0403.</i>
     * <br>
     * Access information about the pixel format of the framebuffer data,
     * being transmitted to the MirrorLink Client.
     *
     * @return The pixel format of the framebuffer data. A Bundle with the fields defined
     * in {@link Defs.ClientPixelFormat}.
     */
    Bundle getClientPixelFormat();

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
