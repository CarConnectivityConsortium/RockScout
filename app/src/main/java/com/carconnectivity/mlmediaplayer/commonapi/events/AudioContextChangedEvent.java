package com.carconnectivity.mlmediaplayer.commonapi.events;

import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderPlaybackState;
import com.carconnectivity.mlmediaplayer.utils.event.RockScoutEvent;

/**
 * Created by sebastian.sokolowski on 20.06.16.
 */
public final class AudioContextChangedEvent implements RockScoutEvent {
    /**
     * Latest playback state
     */
    public final ProviderPlaybackState state;

    public AudioContextChangedEvent
            (ProviderPlaybackState state
            ) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null.");
        }

        this.state = state;
    }
}
