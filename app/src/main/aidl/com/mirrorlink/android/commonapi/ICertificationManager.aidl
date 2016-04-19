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

/**
 * Provides the interface related to 4.3 Certification Information.
 * <br>
 * This is the interface for callbacks coming from {@link ICertificationListener}.
 *
 * <br>
 * <i>Module reference: 0x02</i>
 * <br>
 * <i>Server requirement: Mandatory</i>
 */
interface ICertificationManager {

    /**
     * 4.3.1 Get Application Certification Status.
     *
     * <br>
     * <i>Function reference 0x0201.</i>
     * <br>
     * Provided application certificate status, as captured from the application certificate.
     *
     * @return A bundle detailing {@link Defs.ApplicationCertificationStatus}.
     */
    Bundle getApplicationCertificationStatus();

    /**
     * 4.3.2 Get Application Certifying Entities.
     *
     * <br>
     * <i>Function reference 0x0202.</i>
     * <br>
     * Provide information on the certifying entities.
     *
     * @return Comma-separated list of certifying entities, which certified the application,
     */
    String getApplicationCertifyingEntities();

    /**
     * 4.3.3 Get Application Certification Information.
     *
     * <br>
     * <i>Function reference 0x0203.</i>
     * <br>
     * Provide application certificate information.
     *
     * @param   entity the name of the certifying entity,
     *
     * @return  Bundle containing {@link Defs.CertificateInformation} for the given entity 
     *          or null if the application isn't certified or the entity is not part of the list of
     *          certifying entities for the application,
     */
    Bundle getApplicationCertificationInformation(in String entity);

    /**
     * Notifies the Manager that the application is not using it anymore.
     * <br>
     * Applications are required to call this method when they no longer need to use the Manager.
     * Once called, if at a later point they need the Manager again, they can re-request access to
     * it from the {@link ICommonAPIService}.
     * <br>
     * Once unregister is received by the server, the application will not receive any more
     * callbacks from the Manager.
     */
    oneway void unregister();
}
