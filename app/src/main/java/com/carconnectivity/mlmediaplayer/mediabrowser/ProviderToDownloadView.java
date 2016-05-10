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

import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.carconnectivity.mlmediaplayer.utils.UiUtilities;

import java.net.URL;

/**
 * Created by sebastian.sokolowski on 17/02/16.
 */
public class ProviderToDownloadView {
    final private ProviderDisplayInfo mDisplayInfo;

    public ProviderToDownloadView(
            String label,
            URL icon,
            Uri link) {
        mDisplayInfo = new ProviderDisplayInfo(label, icon, link);
    }

    public ProviderToDownloadView(
            String label,
            Drawable icon) {
        mDisplayInfo = new ProviderDisplayInfo(label, icon);
    }

    public ProviderDisplayInfo getDisplayInfo() {
        return mDisplayInfo;
    }

    public String getUniqueName() {
        return mDisplayInfo.label;
    }

    public boolean hasSameNameAs(ProviderToDownloadView other) {
        return isNameEqual(other.getUniqueName());
    }

    public boolean isNameEqual(String name) {
        return mDisplayInfo.label.equals(name);
    }

    public class ProviderDisplayInfo {
        public final String label;
        public final URL iconUrl;
        public final Drawable iconDraw;
        public final Uri link;
        public final Integer colorPrimaryDark;
        public final Integer colorAccent;

        public static final int DEFAULT_PRIMARY_DARK = 0xff041424;
        public static final int DEFAULT_ACCENT = 0xffea9000;

        public ProviderDisplayInfo(String label,
                                   URL icon,
                                   Uri link) {
            this.label = UiUtilities.trimLabelText(label);
            this.iconUrl = icon;
            this.iconDraw = null;
            this.link = link;
            this.colorPrimaryDark = DEFAULT_PRIMARY_DARK;
            this.colorAccent = DEFAULT_ACCENT;
        }

        public ProviderDisplayInfo(String label, Drawable icon) {
            this.label = UiUtilities.trimLabelText(label);
            this.iconUrl = null;
            this.iconDraw = icon;
            this.link = null;
            this.colorPrimaryDark = DEFAULT_PRIMARY_DARK;
            this.colorAccent = DEFAULT_ACCENT;
        }

        @Override
        public boolean equals(Object that) {
            if (this == that) return true;
            if (that instanceof ProviderToDownloadView) return false;
            return hasSameNameAs((ProviderToDownloadView) that);
        }

        @Override
        public String toString() {
            return "ProviderToDownloadDisplayInfo{" +
                    "label='" + label + '\'' +
                    ", iconUrl=" + (iconUrl != null ? iconUrl.toString() : "null") +
                    ", iconDraw=" + (iconDraw != null ? iconDraw.toString() : "null") +
                    ", link=" + (link != null ? link.toString() : "null") +
                    ", colorPrimaryDark=" + (colorPrimaryDark != null ? Integer.toHexString(colorPrimaryDark) : "null") +
                    ", colorAccent=" + (colorAccent != null ? Integer.toHexString(colorAccent) : "null") +
                    '}';
        }
    }
}
