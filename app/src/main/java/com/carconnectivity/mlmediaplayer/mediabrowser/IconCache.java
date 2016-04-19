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

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by belickim on 09/07/15.
 */
public final class IconCache {
    private final HashMap<Integer, WeakReference<Drawable>> mDrawables;

    public IconCache() {
        mDrawables = new HashMap<>();
    }

    public void addResource(int id, Drawable icon) {
        mDrawables.put(id, new WeakReference<Drawable>(icon));
    }

    public boolean hasResource(int id) {
        if (mDrawables.containsKey(id)) {
            WeakReference<Drawable> maybeDrawable = mDrawables.get(id);
            return maybeDrawable.get() != null;
        }
        return false;
    }

    public Drawable getResource(int id) {
        if (hasResource(id) == false) {
            throw new IllegalStateException("Given resource is missing.");
        }
        return mDrawables.get(id).get();
    }

    public void reset() {
        mDrawables.clear();
    }
}
