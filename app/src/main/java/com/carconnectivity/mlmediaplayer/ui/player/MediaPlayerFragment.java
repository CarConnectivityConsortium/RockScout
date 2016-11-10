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

package com.carconnectivity.mlmediaplayer.ui.player;

import android.app.Fragment;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderPlaybackState;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderViewActive;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.MediaButtonClickedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.MediaExtrasChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.MediaMetadataChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.NowPlayingProviderChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.PlaybackStateChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProgressUpdateEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderConnectErrorEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.MediaButtonData;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.TrackMetadata;
import com.carconnectivity.mlmediaplayer.ui.BackButtonHandler;
import com.carconnectivity.mlmediaplayer.ui.MainActivity;
import com.carconnectivity.mlmediaplayer.utils.PlaybackUtils;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;
import com.carconnectivity.mlmediaplayer.utils.UiUtilities;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Fragment for handling music playback controls
 */
public final class MediaPlayerFragment extends Fragment implements BackButtonHandler {
    private static final String TAG = MediaPlayerFragment.class.getSimpleName();

    private static final int WAIT_INDICATOR_ANIMATION_TIME_SPIN = 1000;
    private static final int WAIT_INDICATOR_ANIMATION_TIME_FADE = 200;

    private TextView mProviderName;
    private ImageView mAlbumArt;
    private TextView mSongTitle;
    private TextView mSongArtist;
    private ImageButton mLauncherButton;
    private ImageButton mNavigatorButton;

    private MediaButton mMediaButton1;
    private MediaButton mMediaButton2;
    private MediaButton mMediaButton3;
    private MediaButton mMediaButton3Sec;
    private MediaButton mMediaButton4;
    private MediaButton mMediaButton5;

    private ImageView mWaitIndicatorFront;
    private ImageView mWaitIndicatorSpin;
    private ImageView mWaitIndicatorBack;

    private ColorStateList mSecondaryToolbarColor;
    private ImageView mToolbarTopImage;
    private ColorStateList mToolbarTopImageColorState;
    private ImageView mToolbarBottomImage;
    private ColorStateList mToolbarBottomImageColorState;
    private ImageView mProgressBar;
    private ProgressTimer mProgressTimer;

    private ProviderPlaybackState mCurrentPlaybackState;
    private long mCurrentTrackDuration;
    private boolean mSecondaryToolbarShown;
    private View mRootView;

    private ProviderViewActive mNowPlayingProvider;

    public static MediaPlayerFragment newInstance() {
        MediaPlayerFragment fragment = new MediaPlayerFragment();
        return fragment;
    }

    @Override
    public View onCreateView
            (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mRootView = inflater.inflate(R.layout.c4_fragment_media_player, container, false);

        mAlbumArt = (ImageView) mRootView.findViewById(R.id.album_art);
        mSongTitle = (TextView) mRootView.findViewById(R.id.song_title);
        mSongArtist = (TextView) mRootView.findViewById(R.id.song_artist);
        mProviderName = (TextView) mRootView.findViewById(R.id.textProviderName);

        mNavigatorButton = (ImageButton) mRootView.findViewById(R.id.buttonNavigator);
        mNavigatorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).openNavigator(null);
            }
        });
        mLauncherButton = (ImageButton) mRootView.findViewById(R.id.buttonLauncher);
        mLauncherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).openLauncher(null);
            }
        });

        mMediaButton1 = (MediaButton) mRootView.findViewById(R.id.mediaButton1);
        mMediaButton2 = (MediaButton) mRootView.findViewById(R.id.mediaButton2);
        mMediaButton3 = (MediaButton) mRootView.findViewById(R.id.mediaButton3);
        mMediaButton3Sec = (MediaButton) mRootView.findViewById(R.id.mediaButton3Sec);
        mMediaButton4 = (MediaButton) mRootView.findViewById(R.id.mediaButton4);
        mMediaButton5 = (MediaButton) mRootView.findViewById(R.id.mediaButton5);

        mWaitIndicatorFront = (ImageView) mRootView.findViewById(R.id.waitIndicatorFront);
        mWaitIndicatorSpin = (ImageView) mRootView.findViewById(R.id.waitIndicatorSpin);
        mWaitIndicatorBack = (ImageView) mRootView.findViewById(R.id.waitIndicatorBack);

        mToolbarTopImage = (ImageView) mRootView.findViewById(R.id.toolbar_top);
        mToolbarBottomImage = (ImageView) mRootView.findViewById(R.id.toolbar_bottom);
        mProgressBar = (ImageView) mRootView.findViewById(R.id.toolbar_progress);

        initializeView();
        enablePrimaryToolbar();

        final MainActivity activity = (MainActivity) getActivity();
        mNowPlayingProvider = activity.getNowPlayingProvider();
        if (mNowPlayingProvider != null) {
            mCurrentPlaybackState
                    = mNowPlayingProvider.getCurrentPlaybackState();
            customizeTheme(mNowPlayingProvider);
            updateMetadata(mNowPlayingProvider.getCurrentMetadata());
            updateMediaButtonsData();
        }

        return mRootView;
    }

    private void initializeView() {
        Log.d(TAG, "initializeView");
        if (mRootView == null) return;
        final Resources resources = getResources();

        final int defaultButtonColor = resources.getColor(R.color.player_background);
        mMediaButton1.setup((ImageView) mRootView.findViewById(R.id.mediaButton1Background), defaultButtonColor);
        mMediaButton2.setup((ImageView) mRootView.findViewById(R.id.mediaButton2Background), defaultButtonColor);
        mMediaButton3.setup((ImageView) mRootView.findViewById(R.id.mediaButton3Background), defaultButtonColor);
        mMediaButton3Sec.setup((ImageView) mRootView.findViewById(R.id.mediaButton3SecBackground), defaultButtonColor);
        mMediaButton3Sec.setVisibility(ImageView.INVISIBLE);
        mMediaButton4.setup((ImageView) mRootView.findViewById(R.id.mediaButton4Background), defaultButtonColor);
        mMediaButton5.setup((ImageView) mRootView.findViewById(R.id.mediaButton5Background), defaultButtonColor);
        mMediaButton5.setSwitchModeIconColor(resources.getColor(R.color.c4_list_background));

        View.OnFocusChangeListener listener
                = UiUtilities.defaultOnFocusChangeListener((MainActivity) getActivity());
        mLauncherButton.setOnFocusChangeListener(listener);
        mNavigatorButton.setOnFocusChangeListener(listener);
        mMediaButton1.setOnFocusChangeListener(listener);
        mMediaButton2.setOnFocusChangeListener(listener);
        mMediaButton3.setOnFocusChangeListener(listener);
        mMediaButton3Sec.setOnFocusChangeListener(listener);
        mMediaButton4.setOnFocusChangeListener(listener);
        mMediaButton5.setOnFocusChangeListener(listener);

        mToolbarTopImageColorState = mToolbarTopImage.getImageTintList();
        mToolbarBottomImageColorState = mToolbarBottomImage.getImageTintList();

        mProviderName.setText("");
        mAlbumArt.setImageDrawable(getResources().getDrawable(R.drawable.c4_player_background, null));
        mSongTitle.setText(R.string.press_play_to_start);
        mSongArtist.setText("");
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        customizeTheme(mNowPlayingProvider);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderConnectErrorEvent event) {
        final String title = getResources().getString(R.string.items_missing);
        final TrackMetadata metadata = new TrackMetadata(title, "", 0L, null, null);
        updateMetadata(metadata);
    }

    @SuppressWarnings("unused")
    public void onEvent(NowPlayingProviderChangedEvent event) {
        mNowPlayingProvider = event.provider;
        customizeTheme(mNowPlayingProvider);
    }

    @SuppressWarnings("unused")
    public void onEvent(MediaMetadataChangedEvent event) {
        if (mNowPlayingProvider == null) {
            mNowPlayingProvider = event.provider;
        }

        if (mNowPlayingProvider.hasSameIdAs(event.provider)) {
            updateMetadata(event.metadata);
        }
    }

    private void updateMetadata(TrackMetadata metadata) {
        Log.d(TAG, "updateMetadata=" + (metadata != null ? metadata.toString() : "null"));
        String title = "";
        String artist = "";
        long duration = 0;

        if (mSongTitle == null) return;

        if (metadata == null) {
            title = getResources().getString(R.string.select_track_to_start);
        } else if (metadata != null && metadata.isTitleEmpty()) {
            if (mCurrentPlaybackState != null && checkIfWeCanPlay(mCurrentPlaybackState.state)) {
                title = getResources().getString(R.string.press_play_to_start);
            }
        } else {
            title = metadata.title;
            artist = metadata.artist;
            if (metadata.artBmp != null) {
                loadNewBitmap(metadata.artBmp);
            } else {
                loadNewBitmap(metadata.artUri);
            }
            duration = metadata.duration;
        }

        mSongTitle.setText(UiUtilities.trimLabelText(title));
        mSongArtist.setText(UiUtilities.trimLabelText(artist));
        mCurrentTrackDuration = duration;
        if (mProgressTimer != null) {
            mProgressTimer.setDuration(mCurrentTrackDuration);
        }
    }

    private boolean checkIfWeCanPlay(int state){
        if(PlaybackState.STATE_PAUSED == state ||
                PlaybackState.STATE_STOPPED == state){
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
    public void onEvent(MediaExtrasChangedEvent event) {
        customizeTheme(mNowPlayingProvider);
    }

    @SuppressWarnings("unused")
    public void onEvent(PlaybackStateChangedEvent event) {
        /* Use originating provider as current provider if current is not set. */
        if (mNowPlayingProvider == null) {
            mNowPlayingProvider = event.provider;
        }

        /* React only to events from current player. */
        if (mNowPlayingProvider.hasSameIdAs(event.provider) == false) return;

        mCurrentPlaybackState = event.state;
        switch (mCurrentPlaybackState.state) {
            case PlaybackState.STATE_PLAYING:
                scheduleProgressTimer
                        (event.provider, event.state.position
                                , event.state.lastPositionUpdateTime
                        );
                break;
            case PlaybackState.STATE_SKIPPING_TO_NEXT:
            case PlaybackState.STATE_SKIPPING_TO_PREVIOUS:
                cancelProgressTimer();
                setProgress(0);
                break;
            case PlaybackState.STATE_BUFFERING:
            case PlaybackState.STATE_CONNECTING:
                enablePrimaryToolbar();
                cancelProgressTimer();
                setProgress(0);
                break;
            default:
                cancelProgressTimer();
                setProgress
                        (PlaybackUtils.calculateProgressPercentage
                                (event.state.position
                                        , event.state.lastPositionUpdateTime
                                        , mCurrentTrackDuration
                                )
                        );
                break;
        }
        updateMediaButtonsData();
        final boolean isWaitIndicatorRequired
                = mCurrentPlaybackState.state == PlaybackState.STATE_BUFFERING
                || mCurrentPlaybackState.state == PlaybackState.STATE_CONNECTING;
        enableWaitIndicator(isWaitIndicatorRequired);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ProgressUpdateEvent event) {
        if (mNowPlayingProvider == null) {
            cancelProgressTimer();
            return;
        }
        if (mNowPlayingProvider.hasSameIdAs(event.provider)) {
            setProgress(event.progress);
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(MediaButtonClickedEvent event) {
        switch (event.mediaButtonData.type) {
            case QUEUE:
                ((MainActivity) getActivity()).openNavigator(getView());
                break;
            case MORE_ACTIONS_ON:
                enableSecondaryToolbar();
                break;
            case MORE_ACTIONS_OFF:
                enablePrimaryToolbar();
                break;
            default:
                break;
        }
    }

    private void customizeTheme(ProviderViewActive providerView) {
        Log.d(TAG, "customizeTheme: providerView=" + (providerView != null ? providerView.toString() : "null"));
        if (providerView == null) {
            Log.w(TAG, "Provider view is null, cannot customize theme.");
            return;
        }
        if (mMediaButton1 == null) {
            Log.w(TAG, "Media buttons are null, cannot customize theme.");
            return;
        }

        final Resources resources = getResources();

        final int defaultButtonColor = resources.getColor(R.color.player_background);
        Log.d(TAG, "Updating color to: " + Integer.toHexString(providerView.getColorAccent()));
        mMediaButton1.setHighlightColor(defaultButtonColor);
        mMediaButton2.setHighlightColor(defaultButtonColor);
        mMediaButton3.setHighlightColor(providerView.getColorAccent());
        mMediaButton3Sec.setHighlightColor(defaultButtonColor);
        mMediaButton4.setHighlightColor(defaultButtonColor);
        mMediaButton5.setHighlightColor(defaultButtonColor);
        mMediaButton5.setSwitchModeHighlightColor(providerView.getColorAccent());

        mWaitIndicatorFront.setImageTintList(ColorStateList.valueOf(providerView.getColorAccent()));
        mWaitIndicatorSpin.setImageTintList(ColorStateList.valueOf(providerView.getColorAccent()));

        mProgressBar.setImageTintList(ColorStateList.valueOf(providerView.getColorAccent()));
        mSecondaryToolbarColor = ColorStateList.valueOf(providerView.getColorPrimaryDark());

        Drawable providerDrawable = providerView.getNotificationDrawable();
        if (providerDrawable == null) {
            providerDrawable = providerView.getIconDrawable();
        }
        mNavigatorButton.setImageDrawable(providerDrawable);
    }

    private void loadNewBitmap(String uri) {
        if (uri == null || uri.equals("") || mAlbumArt == null) return;
        Log.d(TAG, "Updating Bitmap to: " + uri);
        Picasso.with(getActivity().getApplicationContext())
                .load(uri)
                .placeholder(R.drawable.c4_player_background)
                .error(R.drawable.c4_player_background)
                .into(mAlbumArt);
    }

    private void loadNewBitmap(Bitmap bitmap) {
        if (bitmap == null) return;
        Log.d(TAG, "Updating Bitmap to: " + bitmap);
        mAlbumArt.setImageBitmap(bitmap);
    }

    private void enablePrimaryToolbar() {
        if (mSecondaryToolbarShown) {
            mSecondaryToolbarShown = false;
            updateMediaButtonsData();
        }
    }

    private void enableSecondaryToolbar() {
        if (!mSecondaryToolbarShown) {
            mSecondaryToolbarShown = true;
            updateMediaButtonsData();
        }
    }

    private void updateMediaButtonsData() {
        if (mCurrentPlaybackState == null) {
            Log.w(TAG, "Failed to update media buttons, current playback " +
                    "state is null.");
            return;
        }
        if (mMediaButton1 == null) {
            Log.w(TAG, "Failed to update media buttons, media buttons are " +
                    "null.");
            return;
        }

        final List<MediaButtonData> mediaButtonDataList
                = new ArrayList<>(mCurrentPlaybackState.mediaButtons);
        // 1. Check if there are more actions then media buttons
        final int maxVal = 4;
        boolean reserveMoreActionButton = mediaButtonDataList.size() > maxVal;
        // 2. Update playback button
        if (!mSecondaryToolbarShown) {
            mMediaButton3.setMediaButtonData(mCurrentPlaybackState.playbackStateButton);
            mMediaButton3.setIconColor(getActivity().getResources().getColor(R.color.c4_list_background));
        }
        // 3. Update more actions media button if reserved
        if (reserveMoreActionButton) {
            MediaButtonData.Type type = mSecondaryToolbarShown ? MediaButtonData.Type.MORE_ACTIONS_OFF : MediaButtonData.Type.MORE_ACTIONS_ON;
            Drawable icon = PlaybackUtils.getDefaultIconForMediaButton(getActivity(), type);
            mMediaButton5.setMediaButtonData(new MediaButtonData(mNowPlayingProvider, type, null, icon, null));
        }
        // 4. Prepare media button list to be updated
        Iterator<MediaButton> buttonList;
        if (mSecondaryToolbarShown) {
            buttonList = Arrays.asList(mMediaButton1, mMediaButton2, mMediaButton3Sec, mMediaButton4).iterator();
        } else if (reserveMoreActionButton) {
            buttonList = Arrays.asList(mMediaButton1, mMediaButton2, mMediaButton4).iterator();
        } else {
            buttonList = Arrays.asList(mMediaButton1, mMediaButton2, mMediaButton4, mMediaButton5).iterator();
        }
        // 5. Finally update prepared media button list with actions based on secondary toolbar flag
        final int startIdx = mSecondaryToolbarShown ? maxVal - 1 : 0;
        for (int i = startIdx; i < mediaButtonDataList.size(); i++) {
            if (buttonList.hasNext()) {
                buttonList.next().setMediaButtonData(mediaButtonDataList.get(i));
            } else {
                break;
            }
        }
        while (buttonList.hasNext()) {
            buttonList.next().setMediaButtonData(MediaButtonData.createEmptyMediaButtonData());
        }
        // 6. Update media buttons color and visibility
        // Change colors of toolbar
        showSecondaryToolBar(mSecondaryToolbarShown);
    }

    private void showSecondaryToolBar(boolean show) {
        Log.d(TAG, "showSecondaryToolBar: show=" + show);
        // Change colors of toolbar
        if (show) {
            mToolbarTopImage.setImageTintList(mSecondaryToolbarColor);
            mToolbarBottomImage.setImageTintList(mSecondaryToolbarColor);
        } else {
            mToolbarTopImage.setImageTintList(mToolbarTopImageColorState);
            mToolbarBottomImage.setImageTintList(mToolbarBottomImageColorState);
        }
        // Set invert mode on buttons
        mMediaButton1.setInvertMode(show);
        mMediaButton2.setInvertMode(show);
        mMediaButton3Sec.setInvertMode(show);
        mMediaButton4.setInvertMode(show);
        mMediaButton5.setInvertMode(show);
        // Update visibility
        final int visibility = show ? ImageView.INVISIBLE : ImageView.VISIBLE;
        mMediaButton3.setVisibility(visibility);
        mMediaButton3Sec.setVisibility(visibility);
        mWaitIndicatorSpin.setVisibility(visibility);
        mWaitIndicatorSpin.setAlpha(show ? 0.0f : 1.0f);
        mWaitIndicatorFront.setVisibility(visibility);
        mWaitIndicatorBack.setVisibility(visibility);
    }

    private void scheduleProgressTimer(ProviderViewActive currentProvider, long pos, long lastUpdateTime) {
        cancelProgressTimer();
        mProgressTimer = new ProgressTimer();

        if (lastUpdateTime == 0) {
            lastUpdateTime = SystemClock.elapsedRealtime();
        }

        mProgressTimer.setPosition(pos, lastUpdateTime);
        mProgressTimer.setDuration(mCurrentTrackDuration);
        mProgressTimer.start(currentProvider);
    }

    private void cancelProgressTimer() {
        if (mProgressTimer != null) {
            mProgressTimer.cancel();
            mProgressTimer.purge();
            mProgressTimer = null;
        }
    }

    private static final int MAX_LEVEL_VALUE = 10000;

    private void setProgress(float progress) {
        Drawable bar = mProgressBar.getDrawable();
        if (bar == null) return;

        if (progress <= 0) {
            bar.setLevel(0);
        } else if (progress >= 1) {
            bar.setLevel(MAX_LEVEL_VALUE);
        } else {
            bar.setLevel((int) (progress * MAX_LEVEL_VALUE));
        }
    }

    private boolean mWaitIndicatorEnabled = false;

    private void enableWaitIndicator(boolean enable) {
        Log.d(TAG, "enableWaitIndicator: enable=" + enable);
        if (mWaitIndicatorEnabled != enable) {
            mWaitIndicatorEnabled = enable;
            if (mMediaButton3.getBackgroundImage().getAnimation() != null) {
                mMediaButton3.getBackgroundImage().getAnimation().cancel();
                mMediaButton3.getBackgroundImage().setAnimation(null);
            }
            if (enable) {
                if (mWaitIndicatorSpin.getWidth() == 0 || mWaitIndicatorSpin.getHeight() == 0) {
                    UiUtilities.performDelayedInUiThread(getActivity(), new Runnable() {
                        @Override
                        public void run() {
                            enableWaitIndicator(true);
                        }
                    }, 100);
                    return;
                }

                RotateAnimation animation = new RotateAnimation(0.0f, 360.0f, mWaitIndicatorSpin.getWidth() / 2.0f, mWaitIndicatorSpin.getHeight() / 2.0f);
                animation.setDuration(WAIT_INDICATOR_ANIMATION_TIME_SPIN);
                animation.setRepeatMode(AlphaAnimation.RESTART);
                animation.setRepeatCount(AlphaAnimation.INFINITE);
                animation.setFillAfter(true);
                mWaitIndicatorSpin.startAnimation(animation);

                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                alphaAnimation.setDuration(WAIT_INDICATOR_ANIMATION_TIME_FADE);
                alphaAnimation.setFillAfter(true);
                mMediaButton3.getBackgroundImage().startAnimation(alphaAnimation);

            } else {
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        if (mWaitIndicatorSpin.getAnimation() != null) {
                            mWaitIndicatorSpin.getAnimation().cancel();
                            mWaitIndicatorSpin.setAnimation(null);
                        }
                    }
                };
                UiUtilities.performDelayedInUiThread(getActivity(), task, WAIT_INDICATOR_ANIMATION_TIME_FADE);

                AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
                alphaAnimation.setDuration(WAIT_INDICATOR_ANIMATION_TIME_FADE);
                alphaAnimation.setFillAfter(true);
                mMediaButton3.getBackgroundImage().startAnimation(alphaAnimation);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        RsEventBus.register(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mRootView = null;
        cancelProgressTimer();
        RsEventBus.unregister(this);
    }

    @Override
    public boolean handleBackButtonPress() {
        if (getFragmentManager().getBackStackEntryCount() != 0) {
            getFragmentManager().popBackStack();
        } else {
            ((MainActivity) getActivity()).openLauncher(null);
        }
        return false;
    }
}
