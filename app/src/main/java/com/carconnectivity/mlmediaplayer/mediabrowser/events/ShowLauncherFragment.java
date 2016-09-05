package com.carconnectivity.mlmediaplayer.mediabrowser.events;

import com.carconnectivity.mlmediaplayer.utils.event.RockScoutEvent;

/**
 * Created by sebastian.sokolowski on 06.04.16.
 */
public class ShowLauncherFragment implements RockScoutEvent {
    public final boolean show;

    public ShowLauncherFragment(boolean show) {
        this.show = show;
    }

    @Override
    public String toString() {
        return "ShowLauncherFragment{" +
                "show=" + show +
                '}';
    }
}
