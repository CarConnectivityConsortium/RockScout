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

package com.carconnectivity.mlmediaplayer.ui.player;

import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderView;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProgressUpdateEvent;
import com.carconnectivity.mlmediaplayer.utils.PlaybackUtils;

import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

/**
 * Timer for updating playback progress
 */
public class ProgressTimer extends Timer {

    public static final int UPDATE_PERIOD = 1000 / 60; /* 60 fps */

    private long mPos;
    private long mDuration;
    private long mLastUpdateTime;

    private final EventBus mBus = EventBus.getDefault();

    public ProgressTimer() {
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    public void setPosition(long pos, long lastUpdateTime) {
        this.mPos = pos;
        this.mLastUpdateTime = lastUpdateTime;
    }

    public void start(final ProviderView provider) {
        scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final float progressPercentage
                        = PlaybackUtils.calculateProgressPercentage(mPos, mLastUpdateTime, mDuration);
                mBus.post(new ProgressUpdateEvent(provider, progressPercentage));
            }
        }, 0, UPDATE_PERIOD);
    }
}
