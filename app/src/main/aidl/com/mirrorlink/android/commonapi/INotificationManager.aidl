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

import com.mirrorlink.android.commonapi.INotificationListener;

/**
 * Provides the interface related to 4.12 Notifications.
 * <br>
 * The callbacks are defined in {@link INotificationListener}.
 *
 * <br>
 * <i>Module reference: 0x0B</i>
 * <br>
 * <i>Server requirement: Optional</i>
 */
interface INotificationManager {
    /**
     * 4.12.1 Notifications Supported.
     *
     * <br>
     * <i>Function reference 0x0B01.</i>
     * <br>
     * Indicate support for UPnP notifications from the application; the MirrorLink Server will
     * issue a NotiAppListUpdate event, to inform the MirrorLink Client that the notification
     * support for this application has changed. Unless otherwise set by the application, the
     * MirrorLink Server MUST assume that the application will not support notifications.
     *
     * @param notificationSupported Flag indicating notification support from the application.
     */
    void setNotificationSupported(in boolean notificationSupported);

    /**
     * 4.12.2 Notifications Enabled.
     *
     * <br>
     * <i>Function reference 0x0B02.</i>
     * <br>
     * Checks whether notifications are enabled for the application from the MirrorLink Server and
     * Client.
     *
     * @return Flag indicating that notifications are enabled from MirrorLink Server and Client for
     * the application.
     */
    boolean getNotificationEnabled();

    /**
     * 4.12.4 Notification Configuration.
     *
     * <br>
     * <i>Function reference 0x0B04.</i>
     * <br>
     * Get configuration information for the notification service, any later change to the provided
     * information MUST be notified via the callback function.
     *
     * @return Bundle containing the notification configuration. The fields available are defined in
     * {@link Defs.NotificationConfiguration}.
     */
    Bundle getNotificationConfiguration();

    /**
     * 4.12.6 Send Notification for client-based Notification UI.
     *
     * <br>
     * <i>Function reference 0x0B06.</i>
     * <br>
     * Send a notification from the application; this notification replaces a previously send notification.
     *
     * @param   title Title of the notification event.
     * @param   body Body of the notification event.
     * @param   iconUrl Url to icon belonging to the notification.
     * @param   actionList List of actions belonging to the notification. The elements are Bundles
     * with the fields defined in {@link Defs.Action}.
     *
     * @return The notification identifier; a Zero value will be returned, if the action was not successful.
     */
     int sendClientNotification(in String title, in String body, in Uri iconUrl, in List<Bundle> actionList);

    /**
     * 4.12.7 Send Notification for VNC-based Notification UI.
     *
     * <br>
     * <i>Function reference 0x0B07.</i>
     * <br>
     * Send a notification from the application; this notification replaces a previously send notification.
     *
     * @return The notification identifier; a Zero value will be returned, if the action was not successful.
     */
    int sendVncNotification();

    /**
     * 4.12.8 Cancel Notification.
     *
     * <br>
     * <i>Function reference 0x0B08.</i>
     * <br>
     * Cancel a notification from the application.
     *
     * @param notificationId Identifier of the notification, which needs to get canceled. 
     */
    boolean cancelNotification(in int notificationId);

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
