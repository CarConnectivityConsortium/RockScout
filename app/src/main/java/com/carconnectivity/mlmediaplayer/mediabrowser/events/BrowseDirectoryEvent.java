/*
 * Copyright Car Connectivity Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You may decide to give the Car Connectivity Consortium input, suggestions
 * or feedback of a technical nature which may be implemented on the
 * Car Connectivity Consortium products (“Feedback”).
 *
 * You agrees that any such Feedback is given on non-confidential
 * basis and Licensee hereby waives any confidentiality restrictions
 * for such Feedback. In addition, Licensee grants to the Car Connectivity Consortium
 * and its affiliates a worldwide, non-exclusive, perpetual, irrevocable,
 * sub-licensable, royalty-free right and license under Licensee’s copyrights to copy,
 * reproduce, modify, create derivative works and directly or indirectly
 * distribute, make available and communicate to public the Feedback
 * in or in connection to any CCC products, software and/or services.
 */

package com.carconnectivity.mlmediaplayer.mediabrowser.events;

import android.content.ComponentName;

/**
 * Created by belickim on 22/04/15.
 */
public final class BrowseDirectoryEvent {
    /** Name of provider which is browsed, cannot be null. */
    public final ComponentName providerName;
    /** Id of the browsed directory, if null root directory is browsed. */
    public final String directoryId;

    /** Creates new instance of BrowseDirectoryEvent
     * @param providerName name of the browsed provider, cannot be null.
     * @param directoryId id of the directory to browse, if null
     *                    the root of given provider will be browsed
     */
    public BrowseDirectoryEvent(ComponentName providerName, String directoryId) {
        if (providerName == null) {
            throw new IllegalArgumentException("Provider name cannot be null.");
        }
        this.providerName = providerName;
        this.directoryId = directoryId;
    }
}
