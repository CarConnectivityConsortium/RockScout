package com.carconnectivity.mlmediaplayer.mediabrowser;

import android.graphics.drawable.Drawable;

import java.net.URL;

/**
 * Created by sebastian.sokolowski on 18.03.16.
 */
public interface ProviderView {
    int DEFAULT_ACCENT = 0xffea9000;
    int DEFAULT_PRIMARY_DARK = 0xff041424;
    int NOTIFICATION_TINT = 0xffffffff;

    String getLabel();

    String getId();

    Drawable getIconDrawable();

    URL getIconURL();

    int getColorPrimaryDark();

    int getColorAccent();

    boolean hasSameIdAs(ProviderView providerView);

    int getType();
}
