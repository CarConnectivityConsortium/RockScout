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

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.util.Log;

import com.carconnectivity.mlmediaplayer.commonapi.events.AudioBlockingEvent;
import com.carconnectivity.mlmediaplayer.commonapi.events.PlaybackFailedEvent;
import com.carconnectivity.mlmediaplayer.commonapi.events.PrepareForPlaybackEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.MediaButtonClickedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.MediaExtrasChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.MediaMetadataChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.PlayMediaItemEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.PlaybackStateChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.TerminateEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.MediaButtonData;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.SlotReservation;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.TrackMetadata;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Responsible for receiving, translating and passing updates from the session
 * and sending actions to session.
 */
public final class ProviderMediaController extends MediaController.Callback {
    private static final String TAG = ProviderMediaController.class.getSimpleName();
    private final Provider mOwner;
    private MediaController mMediaController;
    private ProviderMediaControllerHelper mHelper;
    private boolean mIsListening = false;
    private Set<SlotReservation> mReservedSlots = new HashSet<>();
    private ProviderPlaybackState mCurrentPlaybackState;
    private TrackMetadata mCurrentMetadata;
    private boolean mHandleMediaButtonDataEvents = true;
    private boolean mResumePlaybackOnUnblock = false;

    public ProviderMediaController(Provider owner) {
        mOwner = owner;
        mCurrentMetadata = TrackMetadata.createEmpty();
        mCurrentPlaybackState = ProviderPlaybackState.createEmpty(null);
    }

    private static String getArtistString(MediaMetadata metadata) {
        if (metadata.containsKey(MediaMetadata.METADATA_KEY_ARTIST)) {
            return metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
        }
        if (metadata.containsKey(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)) {
            return metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST);
        }
        if (metadata.containsKey(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)) {
            return metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE);
        }
        return metadata.getString(MediaMetadata.METADATA_KEY_COMPOSER);
    }

    public TrackMetadata getCurrentMetadata() {
        /* try to update metadata before returning */
        if (mMediaController != null) {
            onMetadataChanged(mMediaController.getMetadata());
        }
        return mCurrentMetadata;
    }

    public ProviderPlaybackState getPlaybackState() {
        if (mMediaController != null) {
            onPlaybackStateChanged(mMediaController.getPlaybackState());
        }
        return mCurrentPlaybackState;
    }

    public boolean isPlaying() {
        final int state = mCurrentPlaybackState.state;
        return state == PlaybackState.STATE_PLAYING
                || state == PlaybackState.STATE_SKIPPING_TO_NEXT
                || state == PlaybackState.STATE_SKIPPING_TO_PREVIOUS
                || state == PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM
                ;
    }

    public boolean isPlayingOrPreparing() {
        final int state = mCurrentPlaybackState.state;
        return isPlaying()
                || state == PlaybackState.STATE_BUFFERING
                || state == PlaybackState.STATE_CONNECTING
                ;
    }

    public void forcePause() {
        Log.d(TAG, "forcePause");
        if (mIsListening == false) return;
        final MediaController.TransportControls controls
                = mMediaController.getTransportControls();
        controls.pause();
    }

    public void startListening(Context context, MediaController controller) {
        if (mIsListening) return;

        Log.d(TAG, "Register callback for provider: " + mOwner.getName().getPackageName());
        // Initialize
        this.mMediaController = controller;
        this.mHelper = new ProviderMediaControllerHelper(context, mOwner.getView());
        // Register
        mMediaController.registerCallback(this);
        mIsListening = true;
        // Perform initial update
        updateReservedSlots(mMediaController.getExtras(), false);
        onMetadataChanged(mMediaController.getMetadata());
        onPlaybackStateChanged(mMediaController.getPlaybackState());
        // Finally register to event bus
        RsEventBus.registerSticky(this);
    }

    public void stopListening() {
        if (mIsListening == false) return;

        Log.d(TAG, "Unregister callback for provider: " + mOwner.getName().getPackageName());
        RsEventBus.unregister(this);
        mMediaController.unregisterCallback(this);
        mMediaController = null;
        mIsListening = false;
        mHelper.reset();

        mCurrentMetadata = TrackMetadata.createEmpty();
        mCurrentPlaybackState = ProviderPlaybackState.createEmpty(mOwner.getView());
    }

    @Override
    public void onExtrasChanged(Bundle extras) {
        Log.d(TAG, "Handle extras changed: " + extras.toString());
        RsEventBus.post(new MediaExtrasChangedEvent(extras));
        updateReservedSlots(extras, true);
    }

    @Override
    public void onMetadataChanged(MediaMetadata metadata) {
        final ProviderViewActive view = mOwner.getView();
        if (metadata == null) {
            Log.w(TAG, "Received null metadata from provider.");
            RsEventBus.post(new MediaMetadataChangedEvent(view, TrackMetadata.createEmpty()));
            return;
        }

        Log.d(TAG, "Handle media metadata changed: " + metadata.toString());
        final String title = metadata.containsKey(MediaMetadata.METADATA_KEY_TITLE)
                ? metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
                : metadata.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE);

        final String artist = getArtistString(metadata);
        final Long duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
        String artUri = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI);
        Bitmap artBmp = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);

        if (artUri == null && artBmp == null) {
            artUri = metadata.getString(MediaMetadata.METADATA_KEY_ART_URI);
            artBmp = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
        }

        final TrackMetadata newMetadata = new TrackMetadata(title, artist, duration, artUri, artBmp);
        if (newMetadata.sameAsOther(mCurrentMetadata) == false) {
            RsEventBus.post(new MediaMetadataChangedEvent(view, newMetadata));
        }
        mCurrentMetadata = newMetadata;
    }

    @Override
    public void onPlaybackStateChanged(PlaybackState playbackState) {
        if (playbackState == null) {
            Log.w(TAG, "Received null playback state.");
            return;
        }

        Log.d(TAG, "Handle playback state changed: " + playbackState.toString());
        final int state = playbackState.getState();
        final long position = playbackState.getPosition();
        final long lastPositionUpdateTime = playbackState.getLastPositionUpdateTime();
        final float playbackSpeed = playbackState.getPlaybackSpeed();
        final long activeQueueItemId = playbackState.getActiveQueueItemId();

        if (state == PlaybackState.STATE_ERROR) {
            RsEventBus.post(new PlaybackFailedEvent());
        }

        final MediaButtonData playbackButton = mHelper.resolvePlaybackButton(playbackState);
        final List<MediaButtonData> mediaButtons
                = mHelper.resolveMediaButtons(mReservedSlots, playbackState);

        final ProviderPlaybackState playback = new ProviderPlaybackState
                (state, position, lastPositionUpdateTime
                        , playbackSpeed, activeQueueItemId, playbackButton
                        , mediaButtons
                );
        mCurrentPlaybackState = playback;
        final PlaybackStateChangedEvent event
                = new PlaybackStateChangedEvent(mOwner.getView(), playback);
        RsEventBus.post(event);
    }

    @Override
    public void onSessionDestroyed() {
        Log.d(TAG, "Handle session destroyed");
        mHelper.reset();
    }

    private boolean isOwner(ProviderViewActive other) {
        if (other == null) return false;
        final ComponentName otherName = other.getUniqueName();
        final ComponentName ownerName = mOwner.getName();
        return otherName.equals(ownerName);
    }

    @SuppressWarnings("unused")
    public void onEvent(PlayMediaItemEvent event) {
        Log.d(TAG, "Handle MediaButtonClickedEvent event: " + event.toString());

        if (isOwner(event.provider) == false) return;

        if (mMediaController != null && !event.mediaId.isEmpty()) {
            RsEventBus.post(new PrepareForPlaybackEvent());
            mMediaController.getTransportControls().playFromMediaId(event.mediaId, event.bundle);
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(TerminateEvent event) {
        Log.d(TAG, "Handle TerminateEvent event: " + event.toString());
        if (isPlayingOrPreparing()) {
            forcePause();
        }
        mOwner.disconnect();
    }

    @SuppressWarnings("unused")
    public void onEvent(MediaButtonClickedEvent event) {
        Log.d(TAG, "Handle MediaButtonClickedEvent event: " + event.toString());

        final MediaButtonData data = event.mediaButtonData;
        if (mHandleMediaButtonDataEvents == false) return;
        if (mMediaController == null) return;
        if (data == null) return;

        if (isOwner(data.provider) == false) return;

        final MediaController.TransportControls controls = mMediaController.getTransportControls();
        switch (event.mediaButtonData.type) {
            case SKIP_TO_PREVIOUS:
                controls.skipToPrevious();
                break;

            case REWIND:
                controls.rewind();
                break;

            case FAST_FORWARD:
                controls.fastForward();
                break;

            case SKIP_TO_NEXT:
                controls.skipToNext();
                break;

            case PLAY:
                RsEventBus.post(new PrepareForPlaybackEvent());
                controls.play();
                break;

            case PAUSE:
                controls.pause();
                break;

            case STOP:
                controls.stop();
                break;

            case CUSTOM:
                controls.sendCustomAction(data.action, data.extras);
                break;
        }
    }

    public void startAudioBlocking() {
        /* setting this to false will disable handling media buttons after audio blocking */
        mHandleMediaButtonDataEvents = true;
        if (mMediaController != null && isPlaying()) {
            mResumePlaybackOnUnblock = true;
            forcePause();
        }
    }

    public void stopAudioBlocking() {
        mHandleMediaButtonDataEvents = true;
        if (mIsListening == false) return;
        if (mResumePlaybackOnUnblock && mMediaController != null) {
            final MediaController.TransportControls controls
                    = mMediaController.getTransportControls();
            controls.play();
            mResumePlaybackOnUnblock = false;
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(AudioBlockingEvent event) {
        if (event.isAudioBlocked) {
            startAudioBlocking();
        } else {
            stopAudioBlocking();
        }
    }

    private void updateReservedSlots(Bundle sessionBundle, boolean updatePlaybackState) {
        if (sessionBundle != null) {
            final Set<SlotReservation> result = new HashSet<>();
            for (SlotReservation slotDef : SlotReservation.values()) {
                String key = slotDef.getKey();
                if (sessionBundle.containsKey(key) && sessionBundle.getBoolean(key)) {
                    result.add(slotDef);
                }
            }
            mReservedSlots = result;
            if (updatePlaybackState) {
                onPlaybackStateChanged(mMediaController.getPlaybackState());
            }
        }
    }
}
