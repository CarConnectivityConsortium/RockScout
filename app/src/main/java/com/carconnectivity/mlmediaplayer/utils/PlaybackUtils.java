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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.MediaButtonData;

/**
 * Utility class for calculating playback progress
 */
public class PlaybackUtils {

    public static float calculateProgressPercentage(long lastPos, long lastPosUpdateTime, long duration) {
        if (duration > 0) {
            long currentTime = SystemClock.elapsedRealtime();
            long progress = currentTime - lastPosUpdateTime + lastPos;
            return (float) progress / duration;
        }
        return 0;
    }

    public static Drawable getDefaultIconForMediaButton(Context context, MediaButtonData.Type mediaButtonType) {
        int iconId = 0;
        switch (mediaButtonType) {
            case QUEUE:
                iconId = R.drawable.ic_btn_navigator;
                break;

            case SKIP_TO_PREVIOUS:
                iconId = R.drawable.ic_btn_skip_to_prev;
                break;

            case REWIND:
                iconId = android.R.drawable.ic_media_rew;
                break;

            case PLAY:
                iconId = R.drawable.ic_btn_play;
                break;

            case PAUSE:
                iconId = R.drawable.ic_btn_pause;
                break;

            case STOP:
                iconId = R.drawable.ic_btn_stop;
                break;

            case FAST_FORWARD:
                iconId = android.R.drawable.ic_media_ff;
                break;

            case SKIP_TO_NEXT:
                iconId = R.drawable.ic_btn_skip_to_next;
                break;

            case MORE_ACTIONS_ON:
            case MORE_ACTIONS_OFF:
                iconId = R.drawable.ic_btn_more_actions;
                break;
        }

        if (iconId != 0) {
            return context.getResources().getDrawable(iconId, null);
        }
        return null;
    }
}
