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

package com.carconnectivity.mlmediaplayer.mediabrowser;

import android.media.session.PlaybackState;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.MediaButtonData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mateusz on 03/10/15.
 */
public class ProviderPlaybackState {
    /**
     * Get current playback state as received from
     * https://developer.android.com/reference/android/media/session/PlaybackState.html#getState()
     */
    public final int state;

    /**
     * Get current playback position in ms.
     */
    public final long position;

    /**
     * Get time of last position update (for progress tracking)
     */
    public final long lastPositionUpdateTime;

    /**
     * Get current playback speed as defined in
     * https://developer.android.com/reference/android/media/session/PlaybackState.html#getPlaybackSpeed()
     */
    public final float playbackSpeed;

    public final long activeQueueItemId;

    /**
     * Get main playback state button data. It may be play, pause or stop action.
     */
    public final MediaButtonData playbackStateButton;

    /**
     * Get current list of media buttons reflecting currently available playback actions.
     * MediaButtonData is stored in list in order in which they should be assigned in UI.
     * List is created taking into account: general action ids order, reserved slots
     * and custom actions.
     */
    public final List<MediaButtonData> mediaButtons;

    public ProviderPlaybackState
            ( int state
            , long position
            , long lastPositionUpdateTime
            , float playbackSpeed
            , long activeQueueItemId
            , MediaButtonData playbackStateButton
            , List<MediaButtonData> mediaButtons
            ) {
        if (playbackStateButton == null) {
            /* inferred from usage: there are no null checks, hence this should never be null */
            throw new IllegalArgumentException("Playback state button cannot be null.");
        }
        if (mediaButtons == null) {
            throw new IllegalArgumentException("Media buttons cannot be null.");
        }

        this.state = state;
        this.position = position;
        this.lastPositionUpdateTime = lastPositionUpdateTime;
        this.playbackSpeed = playbackSpeed;
        this.activeQueueItemId = activeQueueItemId;
        this.playbackStateButton = playbackStateButton;
        /* We need a new array, otherwise the getter will leak mutable reference to the original
         * list, we also need unmodifiable list to prevent changing the copy owned by the event */
        this.mediaButtons = Collections.unmodifiableList(new ArrayList<>(mediaButtons));
    }

    public static ProviderPlaybackState createEmpty(ProviderViewActive view) {
        final MediaButtonData playButtonData
                = new MediaButtonData
                (view, MediaButtonData.Type.PLAY, "play", null, null);
        return new ProviderPlaybackState
                ( PlaybackState.STATE_NONE, 0L, 0L, 1.0f, 0L
                , playButtonData
                , new ArrayList<MediaButtonData>()
                );
    }

    @Override
    public String toString() {
        return "ProviderPlaybackState{" +
                "state=" + state +
                ", position=" + position +
                ", lastPositionUpdateTime=" + lastPositionUpdateTime +
                ", playbackSpeed=" + playbackSpeed +
                ", activeQueueItemId=" + activeQueueItemId +
                ", playbackStateButton=" + playbackStateButton +
                ", mediaButtons=" + mediaButtons +
                '}';
    }
}
