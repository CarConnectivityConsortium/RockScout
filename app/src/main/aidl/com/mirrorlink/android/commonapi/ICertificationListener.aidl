/* Copyright 2013-2014 RealVNC ltd.
 * Portions Copyright 2011-2014 Car Connectivity Consortium LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mirrorlink.android.commonapi;

import android.os.Bundle;

/** Provides the interface related to 4.3 Certification Information - Callbacks.
 * <br>
 * This is the interface for callbacks coming from {@link ICertificationManager}.
 */
oneway interface ICertificationListener {

    /** Indicate that the application certification status has changed.
     * <br>
     * <i>Function reference 0x0204.</i>
     * <br>
     * The application would receive this callback if, for example, the certification status changes
     * when the certificate is revoked.
     * <br>
     * The application should use the calls in the {@link ICertificationManager} to find the latest
     * certification details.
     */
    void onCertificationStatusChanged();

}
