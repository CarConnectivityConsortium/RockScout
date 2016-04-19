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
 * Car Connectivity Consortium products ("Feedback").
 *
 * You agrees that any such Feedback is given on non-confidential
 * basis and Licensee hereby waives any confidentiality restrictions
 * for such Feedback. In addition, Licensee grants to the Car Connectivity Consortium
 * and its affiliates a worldwide, non-exclusive, perpetual, irrevocable,
 * sub-licensable, royalty-free right and license under Licensee's copyrights to copy,
 * reproduce, modify, create derivative works and directly or indirectly
 * distribute, make available and communicate to public the Feedback
 * in or in connection to any CCC products, software and/or services.
 */

package com.carconnectivity.mlmediaplayer.mediabrowser;

import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import android.media.session.PlaybackState;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.TrackMetadata;
import com.carconnectivity.mlmediaplayer.utils.UiUtilities;

/**
 * Created by belickim on 16/04/15.
 */
public class ProviderView {
    final private Provider mProvider;
    final private ProviderDisplayInfo mDisplayInfo;

    public ProviderView(Provider provider,
                        String label,
                        Drawable icon,
                        Integer colorPrimaryDark,
                        Integer colorAccent,
                        Drawable notificationDrawable) {
        if (provider == null) {
            throw new IllegalArgumentException("Provider cannot be null.");
        }
        mProvider = provider;
        mDisplayInfo = new ProviderDisplayInfo(label, icon, colorPrimaryDark, colorAccent, notificationDrawable);
    }

    public ProviderDisplayInfo getDisplayInfo() {
        return mDisplayInfo;
    }

    public TrackMetadata getCurrentMetadata() {
        return mProvider.getCurrentMetadata();
    }

    public ProviderPlaybackState getCurrentPlaybackState() {
        return mProvider.getPlaybackState();
    }

    public ComponentName getUniqueName() {
        return mProvider.getName();
    }

    public boolean canConnect() {
        return mProvider.canConnect();
    }

    public boolean isConnected() {
        return mProvider.isConnected();
    }

    public boolean hasSameNameAs(ProviderView other) {
        return isNameEqual(other.getUniqueName());
    }

    public boolean isNameEqual(ComponentName name) {
        return mProvider.isNameEqual(name);
    }

    public class ProviderDisplayInfo {
        public final String label;
        public final Drawable icon;
        public final Integer colorPrimaryDark;
        public final Integer colorAccent;
        public final Drawable notificationDrawable;

        public static final int NOTIFICATION_TINT = 0xffffffff;
        public static final int DEFAULT_PRIMARY_DARK = 0xff041424;
        public static final int DEFAULT_ACCENT = 0xffea9000;

        public ProviderDisplayInfo(String label,
                                   Drawable icon,
                                   Integer colorPrimaryDark,
                                   Integer colorAccent,
                                   Drawable notificationDrawable) {
            this.label = UiUtilities.trimLabelText(label);
            this.icon = icon;
            this.notificationDrawable = notificationDrawable;
            this.colorPrimaryDark = getPrimaryDarkColor(colorPrimaryDark);
            this.colorAccent = getAccentColor(colorAccent);

            if (this.notificationDrawable != null) {
                this.notificationDrawable.setTint(NOTIFICATION_TINT);
            }
        }

        private int getAccentColor(int color) {
            if (UiUtilities.isContrastRequirementMet(color, DEFAULT_PRIMARY_DARK)) {
                return color;
            }
            return DEFAULT_ACCENT;
        }

        private int getPrimaryDarkColor(int color) {
            if (UiUtilities.isContrastRequirementMet(color, Color.WHITE)) {
                return color;
            }
            return DEFAULT_PRIMARY_DARK;
        }

        @Override
        public boolean equals(Object that){
            if (this == that) return true;
            if (that instanceof ProviderView) return false;
            return hasSameNameAs((ProviderView)that);
        }

        @Override
        public String toString() {
            return "ProviderDisplayInfo{" +
                    "label='" + label + '\'' +
                    ", icon=" + icon +
                    ", colorPrimaryDark=" + (colorPrimaryDark != null ? Integer.toHexString(colorPrimaryDark) : "null") +
                    ", colorAccent=" + (colorAccent != null ? Integer.toHexString(colorAccent) : "null") +
                    ", notificationDrawable=" + notificationDrawable +
                    '}';
        }
    }
}
