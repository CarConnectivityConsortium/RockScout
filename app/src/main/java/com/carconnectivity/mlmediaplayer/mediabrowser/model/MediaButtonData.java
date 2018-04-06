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

import android.graphics.drawable.Drawable;
import android.media.session.PlaybackState;
import android.os.Bundle;

import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderViewActive;

/**
 * Holds information about media button specific data, like type of button and, custom action.
 */
public class MediaButtonData {

    public final ProviderViewActive provider;
    public final Type type;
    public final String action;
    public final Drawable icon;
    public final Bundle extras;
    public MediaButtonData
            (ProviderViewActive provider
                    , Type type
                    , String action
                    , Drawable icon
                    , Bundle extras
            ) {
        this.provider = provider;
        this.type = type;
        this.action = action;
        this.icon = icon;
        this.extras = extras;
    }

    public static MediaButtonData createEmptyMediaButtonData() {
        return new MediaButtonData(null, Type.EMPTY, null, null, null);
    }

    @Override
    public String toString() {
        return "MediaButtonData{" +
                "type=" + type +
                ", action='" + action + '\'' +
                ", icon=" + icon +
                ", extras=" + extras +
                '}';
    }

    /**
     * Relates to available, supported actions that can be set in
     * PlaybackState.Builder setActions (long actions)
     */
    public enum Type {
        EMPTY(0),
        QUEUE(0),
        SKIP_TO_PREVIOUS(PlaybackState.ACTION_SKIP_TO_PREVIOUS),
        REWIND(PlaybackState.ACTION_REWIND),
        PLAY(PlaybackState.ACTION_PLAY),
        PAUSE(PlaybackState.ACTION_PAUSE),
        STOP(PlaybackState.ACTION_STOP),
        FAST_FORWARD(PlaybackState.ACTION_FAST_FORWARD),
        SKIP_TO_NEXT(PlaybackState.ACTION_SKIP_TO_NEXT),
        CUSTOM(0),
        MORE_ACTIONS_ON(0),
        MORE_ACTIONS_OFF(0);

        private long mActionId;

        Type(long actionId) {
            this.mActionId = actionId;
        }

        /**
         * Returns type based on the provided actionId
         *
         * @param actionId must be greater than 0
         */
        public static Type fromActionId(long actionId) {
            if (actionId != 0) {
                for (Type type : values()) {
                    if (type.getActionId() == actionId) {
                        return type;
                    }
                }
            }
            return null;
        }

        public long getActionId() {
            return mActionId;
        }
    }
}
