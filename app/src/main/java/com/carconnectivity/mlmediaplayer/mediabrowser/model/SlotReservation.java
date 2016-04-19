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

package com.carconnectivity.mlmediaplayer.mediabrowser.model;

import android.media.session.PlaybackState;

/**
 * Slot reservation for media buttons in UI.
 */
public enum SlotReservation {

    QUEUE(0, 0, "com.google.android.gms.car.media.ALWAYS_RESERVE_SPACE_FOR.ACTION_QUEUE"),
    SKIP_TO_PREV(1, PlaybackState.ACTION_SKIP_TO_PREVIOUS, "com.google.android.gms.car.media.ALWAYS_RESERVE_SPACE_FOR.ACTION_SKIP_TO_PREVIOUS"),
    SKIP_TO_NEXT(2, PlaybackState.ACTION_SKIP_TO_NEXT, "com.google.android.gms.car.media.ALWAYS_RESERVE_SPACE_FOR.ACTION_SKIP_TO_NEXT");

    public static final int LAST_SLOT_POSITION_VALUE = 2;

    private int mPosition;
    private long mActionId;
    private String mKey;

    SlotReservation(int position, long actionId, String key) {
        this.mPosition = position;
        this.mActionId = actionId;
        this.mKey = key;
    }

    public int getPosition() {
        return mPosition;
    }

    public long getActionId() {
        return mActionId;
    }

    public String getKey() {
        return mKey;
    }

    @Override
    public String toString() {
        return "SlotReservation{" +
                "position=" + mPosition +
                ", actionId=" + mActionId +
                ", name='" + mKey + '\'' +
                '}';
    }
}
