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
 * Provides the interface related to 4.9 Context Information - Callbacks.
 * <br>
 * This is the interface for callbacks coming from {@link IContextManager}.
 */
oneway interface IContextListener {

    /**
     * 4.9.2 Framebuffer Blocking Information Callback.
     *
     * <br>
     * <i>Function reference 0x0802.</i>
     * <br>
     * Framebuffer is blocked from the MirrorLink Client; in case the application has indicated that
     * it will handle the blocking it MUST remove the blocked content.
     * <br>
     * Sometimes the application MUST switch to a new view and update its context information, other
     * times there is nothing the application can do to help unblock the framebuffer. For details of
     * reasons of blocking and what the application is required to do see {@link
     * Defs.BlockingInformation}.
     * <br>
     * The Server SHOULD only pass the framebuffer blocking notification to the application only if
     * no reason flag is set to handle the blocking by itself.
     *
     * @param reason Reason for Framebuffer blocking. Will have a value defined in {@link
     * Defs.BlockingInformation}.  Note: Blocking because of the wrong
     * framebuffer orientation, is not reported via this function.
     * @param framebufferArea Framebuffer rectangle for the specified region. The values available
     * are defined in {@link Defs.Rect}.
     *
     * @see #onFramebufferUnblocked
     */
    void onFramebufferBlocked(in int reason, in Bundle framebufferArea);

    /**
     * 4.9.4 Audio Blocking Information.
     *
     * <br>
     * <i>Function reference 0x0804.</i>
     * <br>
     * Audio is blocked from the MirrorLink Client; in case the application has indicated that
     * it will handle the blocking it MUST remove the blocked content.
     * <br>
     * The Server SHOULD only pass the audio blocking notification to the application only if
     * no reason flag is set to handle the blocking by itself.
     * <br>
     * If handling the audio blocking, the MirrorLink Server MUST either filter the application's
     * audio, or terminate the application.
     * <br>
     * If the application is handling the blocking but it continues to stream after being notified,
     * then the Server MAY terminate the application.
     *
     * @param reason Reason for Audio blocking. Will have a value defined in {@link
     * Defs.BlockingInformation}. The reason MUST be different from 0 as reason 0 means that the
     * audio is unblocked, that is reported via {@link #onAudioUnblocked}.
     *
     * @see #onAudioUnblocked
     */
    void onAudioBlocked(in int reason);

    /**
     * 4.9.5 Framebuffer Unblocking Callback
     * <br>
     * <i>Function reference 0x0805.</i>
     * <br>
     * Framebuffer is unblocked from the MirrorLink Client. There can be various ways to unblock the
     * framebuffer, depending on the blocking reasons. See {@link Defs.BlockingInformation} for
     * details.
     * <p>
     * If the framebuffer was blocked with more than one reason, all the reasons must be resolved
     * before this callback will be issued.
     * <br>
     * The Server SHOULD only pass the framebuffer unblocking notification to the application only if
     * no reason flag was set to handle the blocking by itself.
     *
     * @see #onFramebufferBlocked
     */
    void onFramebufferUnblocked();

    /**
     * 4.9.6 Audio Unblocking Callback
     * <br>
     * <i>Function reference 0x0806.</i>
     * <br>
     * Audio is unblocked from the MirrorLink Client. This signal will be emitted, if the
     * MirrorLink Client has previously blocked application's audio stream. The application will
     * receive this signal, as soon as the MirrorLink Client resumes the audio.
     * <br>
     * The Server SHOULD only pass the audio unblocking notification to the application only if
     * no reason flag was set to handle the blocking by itself.
     *
     * @see #onAudioBlocked
     */
    void onAudioUnblocked();
}
