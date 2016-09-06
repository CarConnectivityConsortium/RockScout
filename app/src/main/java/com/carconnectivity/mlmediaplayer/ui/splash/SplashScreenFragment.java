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

package com.carconnectivity.mlmediaplayer.ui.splash;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.AnimateAlphaEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderConnectedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderDiscoveryFinished;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ShowLauncherFragment;
import com.carconnectivity.mlmediaplayer.ui.BackButtonHandler;
import com.carconnectivity.mlmediaplayer.ui.InteractionListener;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class SplashScreenFragment extends Fragment implements BackButtonHandler {
    public static final int TIMER_PERIOD = 5;
    public static final float ALPHA_STEP = 0.001f;
    private static final String TAG = SplashScreenFragment.class.getSimpleName();

    private FrameLayout mSplashImage;
    private float mSplashAlpha;
    private Timer mTimer;

    private WeakReference<InteractionListener> mListener;

    private boolean mDiscoveryOver = false;
    private boolean mTimerFinished = false;
    private boolean mLaunchedAlreadyPlaying = false;
    private boolean mSplashShown = true;

    public static SplashScreenFragment newInstance() {
        SplashScreenFragment fragment = new SplashScreenFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        RsEventBus.register(fragment);

        return fragment;
    }

    public SplashScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSplashShown = true;
        checkAndHide();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSplashShown = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RsEventBus.unregister(this);
    }

    @Override
    public boolean handleBackButtonPress() {
        return false;
    }

    private void checkAndHide() {
        if (mDiscoveryOver && mTimerFinished && mListener != null
                && mSplashShown) {
            if (mLaunchedAlreadyPlaying) {
                mListener.get().showMediaPlayer();
            } else {
                mListener.get().showLauncher();
            }

            mSplashShown = false;
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(ShowLauncherFragment event) {
        if (event.show && mListener != null) {
            mTimer.cancel();
            if (mLaunchedAlreadyPlaying) {
                mListener.get().showMediaPlayer();
            } else {
                mListener.get().showLauncher();
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderConnectedEvent event) {
        if (event.componentName == null) return;
        if (event.showPlayer) {
            mLaunchedAlreadyPlaying = true;
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderDiscoveryFinished event) {
        mDiscoveryOver = true;
        checkAndHide();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(AnimateAlphaEvent event) {
        mSplashImage.setAlpha(mSplashAlpha);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.c4_fragment_splash, container, false);

        mSplashImage = (FrameLayout) root.findViewById(R.id.splash_screen_image);
        mSplashAlpha = 0.0f;

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mSplashAlpha += ALPHA_STEP; /* TODO: nonlinear easing would look much better */
                if (mSplashAlpha < 1.0f) {
                    RsEventBus.post(new AnimateAlphaEvent(mSplashAlpha));
                } else {
                    mTimerFinished = true;
                    mTimer.cancel();
                    checkAndHide();
                }
            }
        }, 0, TIMER_PERIOD);

        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = new WeakReference<InteractionListener>((InteractionListener) activity);
            checkAndHide();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
