package com.carconnectivity.mlmediaplayer.mediabrowser.events;

import com.carconnectivity.mlmediaplayer.utils.event.RockScoutEvent;

/**
 * Created by sebastian.sokolowski on 02.03.16.
 */
public class AnimateAlphaEvent implements RockScoutEvent {
    public final float currentAlphaLevel;

    public AnimateAlphaEvent(float alphaLevel) {
        this.currentAlphaLevel = alphaLevel;
    }

    @Override
    public String toString() {
        return "AnimateAlphaEvent{" +
                "currentAlphaLevel=" + currentAlphaLevel +
                '}';
    }
}
