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

import com.carconnectivity.mlmediaplayer.mediabrowser.MediaItemView;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by belickim on 20/04/15.
 */
public final class ProviderBrowseSuccessfulEvent {
    /** Currently browsed provider */
    public final ProviderView provider;

    /** Parent id of the successfully browsed directory, cannot be null. */
    public final String parentId;

    /** List of items in the browsed directory, cannot be null. */
    public final List<MediaItemView> items;

    public ProviderBrowseSuccessfulEvent
            ( ProviderView provider, String parentId
            , Collection<MediaItemView> items
            ) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null.");
        }
        if (parentId == null) {
            throw new IllegalArgumentException("Parent id cannot be null.");
        }
        if (items == null) {
            throw new IllegalArgumentException("Items cannot be null.");
        }

        this.provider = provider;
        this.parentId = parentId;
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }

    @Override
    public String toString() {
        return "ProviderBrowseSuccessfulEvent{" +
                "parentId='" + parentId + '\'' +
                ", items=" + items.toString() +
                '}';
    }
}
