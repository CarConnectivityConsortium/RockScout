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
