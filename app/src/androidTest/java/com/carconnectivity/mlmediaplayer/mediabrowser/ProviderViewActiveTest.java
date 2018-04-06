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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.service.media.MediaBrowserService;
import android.test.AndroidTestCase;

import java.util.List;

/**
 * Created by mszafranek on 04.08.16.
 */
public class ProviderViewActiveTest extends AndroidTestCase {

    private List<ResolveInfo> getMediaBrowserPackages() {
        PackageManager mManager = getContext().getPackageManager();

        final Intent intent = new Intent(MediaBrowserService.SERVICE_INTERFACE);
        List<ResolveInfo> resolveInfo = mManager.queryIntentServices(intent, 0);
        if (resolveInfo.size() == 0) {
            throw new IllegalStateException("Media browser packages can not be empty. " +
                    "You must install at least one provider to run testing");
        }
        return resolveInfo;
    }

    public void testCreation() {
        ProvidersManager providersManager = new ProvidersManager(getContext(), getContext().getPackageManager());
        List<ResolveInfo> resolveInfo = getMediaBrowserPackages();
        ProviderViewActive test = new ProviderViewActive(new Provider(providersManager, resolveInfo.get(0), false), null, null, null, 0, 0, null);
        assertNotNull(test);
    }

    public void testCreationThrowException() {
        try {
            ProviderViewActive test = new ProviderViewActive(null, null, null, null, 0, 0, null);
            fail("should throw exception");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

}
