package com.carconnectivity.mlmediaplayer.mediabrowser;

import android.graphics.drawable.Drawable;

import com.carconnectivity.mlmediaplayer.utils.UiUtilities;

import java.net.URL;

/**
 * Created by sebastian.sokolowski on 18.03.16.
 */
public class ProviderViewInactive implements ProviderView {
    private final int order = 2;
    private final String label;
    private final String id;
    private final Drawable iconDrawable;
    private final URL iconURL;
    private final int colorPrimaryDark;
    private final int colorAccent;

    public ProviderViewInactive(String label, String id, Drawable iconDrawable) {
        this.label = UiUtilities.trimLabelText(label);
        this.id = id;
        this.iconDrawable = iconDrawable;
        this.iconURL = null;
        this.colorPrimaryDark = DEFAULT_PRIMARY_DARK;
        this.colorAccent = DEFAULT_ACCENT;
    }

    public ProviderViewInactive(String label, String id, URL iconURL) {
        this.label = UiUtilities.trimLabelText(label);
        this.id = id;
        this.iconDrawable = null;
        this.iconURL = iconURL;
        this.colorPrimaryDark = DEFAULT_PRIMARY_DARK;
        this.colorAccent = DEFAULT_ACCENT;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    @Override
    public URL getIconURL() {
        return iconURL;
    }

    @Override
    public int getColorPrimaryDark() {
        return colorPrimaryDark;
    }

    @Override
    public int getColorAccent() {
        return colorAccent;
    }

    @Override
    public boolean hasSameIdAs(ProviderView providerView) {
        return id.equals(providerView.getId());
    }

    @Override
    public int getType() {
        return order;
    }

    @Override
    public String toString() {
        return "ProviderViewInactive{" +
                "label='" + label + '\'' +
                ", id='" + id + '\'' +
                ", iconDrawable=" + (iconDrawable != null ? iconDrawable.toString() : "null") +
                ", iconURL=" + (iconURL != null ? iconURL.toString() : "null") +
                ", colorPrimaryDark=" + colorPrimaryDark +
                ", colorAccent=" + colorAccent +
                '}';
    }
}
