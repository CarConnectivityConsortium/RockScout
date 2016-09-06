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

package com.carconnectivity.mlmediaplayer.utils;

import android.util.Log;

import com.carconnectivity.mlmediaplayer.mediabrowser.events.AnimateAlphaEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProgressUpdateEvent;
import com.carconnectivity.mlmediaplayer.utils.event.RockScoutEvent;

import de.greenrobot.event.EventBus;

public class RsEventBus {

    private static final String TAG = RsEventBus.class.getSimpleName();

    private static final EventBus mBus = new EventBus();

    public static void register(Object subscriber) {
        if (!mBus.isRegistered(subscriber)) {
            mBus.register(subscriber);
        }
    }

    public static void registerSticky(Object subscriber) {
        if (!mBus.isRegistered(subscriber)) {
            mBus.registerSticky(subscriber);
        }
    }

    public static void post(RockScoutEvent event) {
        if (event instanceof AnimateAlphaEvent == false &&
                event instanceof ProgressUpdateEvent == false) {
            Log.d(TAG, "Post event: " + event.getClass().getSimpleName() + " - " + event);
        }
        mBus.post(event);
    }

    public static void postSticky(RockScoutEvent event) {
        Log.d(TAG, "Post sticky event: " + event.getClass().getSimpleName() + " - " + event);
        mBus.postSticky(event);
    }

    public static void unregister(Object subscriber) {
        mBus.unregister(subscriber);
    }
}
