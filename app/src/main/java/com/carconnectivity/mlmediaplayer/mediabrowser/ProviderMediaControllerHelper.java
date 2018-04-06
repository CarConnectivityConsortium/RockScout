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

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.session.PlaybackState;
import android.util.Log;

import com.carconnectivity.mlmediaplayer.mediabrowser.model.MediaButtonData;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.SlotReservation;
import com.carconnectivity.mlmediaplayer.utils.ImageUtils;
import com.carconnectivity.mlmediaplayer.utils.PlaybackUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Helper class holding logic for media button resolution and drawable resources resolution
 */
public final class ProviderMediaControllerHelper {

    private static final String TAG = ProviderMediaControllerHelper.class.getSimpleName();

    private static final int MAX_ACTIONS_COUNT = 10;
    private static final Long[] SUPPORTED_GENERAL_ACTIONS = new Long[]{
            PlaybackState.ACTION_SKIP_TO_PREVIOUS,
            PlaybackState.ACTION_SKIP_TO_NEXT,
    };
    private final Context mContext;
    private final String mProviderPackage;
    private final ProviderViewActive mBoundProvider;
    private final IconCache mIconCache;
    private Resources mProviderResources;

    public ProviderMediaControllerHelper(Context context, ProviderViewActive provider) {
        this.mContext = context;
        this.mProviderPackage = provider.getId();
        this.mBoundProvider = provider;
        this.mIconCache = new IconCache();
        Log.d(TAG, "ProviderMediaControllerHelper instantiated with provider: " + mProviderPackage);
    }

    public void reset() {
        mProviderResources = null;
        mIconCache.reset();
    }

    public MediaButtonData resolvePlaybackButton(PlaybackState playbackState) {
        // Resolve playback button based on playback state (default is play button)
        // Actions are only used to resolve which of pause or stop buttons should be shown
        long actionId;
        switch (playbackState.getState()) {
            case PlaybackState.STATE_PLAYING:
                if (containsAction(PlaybackState.ACTION_STOP, playbackState)
                        && containsAction(PlaybackState.ACTION_PAUSE, playbackState) == false) {
                    actionId = PlaybackState.ACTION_STOP;
                } else {
                    actionId = PlaybackState.ACTION_PAUSE;
                }
                break;

            default:
                actionId = PlaybackState.ACTION_PLAY;
                break;
        }
        return actionToMediaButton(actionId);
    }

    public List<MediaButtonData> resolveMediaButtons(Set<SlotReservation> reservedSlots, PlaybackState playbackState) {
        List<MediaButtonData> result = new ArrayList<>();
        final Iterator<Long> generalActionsIterator = Arrays.asList(SUPPORTED_GENERAL_ACTIONS).iterator();
        final Iterator<PlaybackState.CustomAction> customActionsIterator =
                playbackState.getCustomActions() != null ? playbackState.getCustomActions().iterator() : null;
        // Logic for button resolution - for each available media button space perform check
        for (int idx = 0; idx < MAX_ACTIONS_COUNT; idx++) {
            // 1. If there is slot reserved for position - check action for slot, if exists put that action,
            //    if not put empty button instead.
            MediaButtonData data = checkReservedSlot(reservedSlots, idx, playbackState);
            // 2. If slot is not reserved - check next general action from defined order
            if (data == null) {
                data = checkGeneralAction(reservedSlots, generalActionsIterator, playbackState, idx);
            }
            // 3. If all general action have been checked, go through custom actions
            if (data == null) {
                data = checkCustomAction(customActionsIterator);
            }
            // 4. Check if media button was resolved, if not end resolution process
            // if there are no more reserved slot positions
            if (data != null) {
                result.add(data);
            } else if (idx <= SlotReservation.LAST_SLOT_POSITION_VALUE) {
                result.add(MediaButtonData.createEmptyMediaButtonData());
            } else {
                break;
            }
        }
        return result;
    }

    private MediaButtonData checkReservedSlot(Set<SlotReservation> reservedSlots, int position, PlaybackState state) {
        SlotReservation reservedSlot = null;
        for (SlotReservation slot : reservedSlots) {
            if (slot.getPosition() == position) {
                reservedSlot = slot;
                break;
            }
        }
        if (reservedSlot != null) {
            if (reservedSlot == SlotReservation.QUEUE || containsAction(reservedSlot.getActionId(), state)) {
                return slotReservationToMediaButton(reservedSlot);
            } else {
                return MediaButtonData.createEmptyMediaButtonData();
            }
        }
        return null;
    }

    private MediaButtonData checkGeneralAction(Set<SlotReservation> reservedSlots,
                                               final Iterator<Long> generalActions,
                                               PlaybackState state, int idx) {
        if (idx == 1) {
            boolean actionReserved = checkIfReserved(reservedSlots, PlaybackState.ACTION_SKIP_TO_PREVIOUS);
            if (!actionReserved && containsAction(PlaybackState.ACTION_SKIP_TO_PREVIOUS, state))
                return actionToMediaButton(PlaybackState.ACTION_SKIP_TO_PREVIOUS);
        } else if (idx == 2) {
            boolean actionReserved = checkIfReserved(reservedSlots, PlaybackState.ACTION_SKIP_TO_NEXT);
            if (!actionReserved && containsAction(PlaybackState.ACTION_SKIP_TO_NEXT, state))
                return actionToMediaButton(PlaybackState.ACTION_SKIP_TO_NEXT);
        }
        while (generalActions.hasNext()) {
            Long generalActionId = generalActions.next();
            if (generalActionId != PlaybackState.ACTION_SKIP_TO_PREVIOUS && generalActionId != PlaybackState.ACTION_SKIP_TO_NEXT) {
                boolean actionReserved = checkIfReserved(reservedSlots, generalActionId);
                if (!actionReserved && containsAction(generalActionId, state)) {
                    return actionToMediaButton(generalActionId);
                }
            }
        }
        return null;
    }

    private boolean checkIfReserved(Set<SlotReservation> reservedSlots, Long generalActionId) {
        boolean actionReserved = false;
        for (SlotReservation slot : reservedSlots) {
            if (slot.getActionId() == generalActionId) {
                actionReserved = true;
                break;
            }
        }
        return actionReserved;
    }


    private MediaButtonData checkCustomAction
            (final Iterator<PlaybackState.CustomAction> customActions) {
        if (customActions != null && customActions.hasNext()) {
            final PlaybackState.CustomAction customAction = customActions.next();
            final Drawable icon = getCustomIconForMediaButton(customAction.getIcon());
            return new MediaButtonData
                    (mBoundProvider, MediaButtonData.Type.CUSTOM
                            , customAction.getAction(), icon, customAction.getExtras()
                    );
        }
        return null;
    }

    private MediaButtonData slotReservationToMediaButton(SlotReservation reservation) {
        switch (reservation) {
            case QUEUE:
                final MediaButtonData.Type buttonType = MediaButtonData.Type.QUEUE;
                return new MediaButtonData
                        (mBoundProvider, buttonType, null
                                , PlaybackUtils.getDefaultIconForMediaButton(mContext, buttonType), null
                        );

            default:
                return actionToMediaButton(reservation.getActionId());
        }
    }

    private MediaButtonData actionToMediaButton(long actionId) {
        MediaButtonData.Type buttonType = MediaButtonData.Type.fromActionId(actionId);
        if (buttonType != null) {
            return new MediaButtonData
                    (mBoundProvider, buttonType, null
                            , PlaybackUtils.getDefaultIconForMediaButton(mContext, buttonType), null
                    );
        }
        return null;
    }

    private Drawable getCustomIconForMediaButton(int drawableId) {
        Log.v(TAG, "Get custom icon from provider resources with id: " + drawableId);
        try {
            if (mProviderResources == null) {
                PackageManager packageManager = mContext.getPackageManager();
                mProviderResources = packageManager.getResourcesForApplication(mProviderPackage);
            }

            Drawable icon = null;
            if (mIconCache.hasResource(drawableId)) {
                icon = mIconCache.getResource(drawableId);
            } else {
                final Drawable rawDrawable = mProviderResources.getDrawable(drawableId, null);
                icon = ImageUtils.trimTransparent(rawDrawable, mProviderResources);
                mIconCache.addResource(drawableId, icon);
            }
            return icon;
        } catch (Resources.NotFoundException nfe) {
            Log.e(TAG, "Drawable " + drawableId + " not found for package: " + mProviderPackage, nfe);
        } catch (PackageManager.NameNotFoundException nnfe) {
            Log.e(TAG, "Resources not found for package: " + mProviderPackage, nnfe);
        }
        return null;
    }

    private boolean containsAction(long actionId, PlaybackState state) {
        return (state.getActions() & actionId) == actionId;
    }
}
