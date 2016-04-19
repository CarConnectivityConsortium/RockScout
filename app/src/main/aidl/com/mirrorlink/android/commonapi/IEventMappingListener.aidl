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
 * Provides the interface related to 4.6 Event Features - Callbacks.
 * <br>
 * This is the interface for callbacks coming from {@link IEventMappingManager}.
 */
oneway interface IEventMappingListener {
    /**
     * 4.6.2 Event Configuration Callback.
     *
     * <br>
     * <i>Function reference 0x0502.</i>
     * <br>
     * MirrorLink session event configuration information has changed.
     * <br>
     * Please note that the knob keys, device keys, multimedia keys, function keys, ITU keys, touch
     * and pressurem mask parameters refer to the what is supported by the Session (by both the
     * Client AND the Server).
     *
     * @param eventConfiguration The event configuration of the MirrorLink session. The fields
     * available in the return type are defined in {@link Defs.EventConfiguration}.
     */
    void onEventConfigurationChanged(in Bundle eventConfiguration);

    /**
     * 4.6.4 Get Event Mapping Callback.
     *
     * <br>
     * <i>Function reference 0x0505.</i>
     * <br>
     * The application MUST be notified, whenever the MirrorLink Server and Client change the mapping.
     *
     * @param eventMapping The mapping information about remote events and local events. This is a
     * Bundle with the fields defined in {@link Defs.EventMapping}.
     */
    void onEventMappingChanged(in Bundle eventMapping);

}
