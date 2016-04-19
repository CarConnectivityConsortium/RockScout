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
 * Car Connectivity Consortium products ("Feedback").
 *
 * You agrees that any such Feedback is given on non-confidential
 * basis and Licensee hereby waives any confidentiality restrictions
 * for such Feedback. In addition, Licensee grants to the Car Connectivity Consortium
 * and its affiliates a worldwide, non-exclusive, perpetual, irrevocable,
 * sub-licensable, royalty-free right and license under Licensee's copyrights to copy,
 * reproduce, modify, create derivative works and directly or indirectly
 * distribute, make available and communicate to public the Feedback
 * in or in connection to any CCC products, software and/or services.
 */

package com.carconnectivity.mlmediaplayer.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.commonapi.MirrorLinkApplicationContext;
import com.carconnectivity.mlmediaplayer.commonapi.MirrorLinkConnectionManager;
import com.carconnectivity.mlmediaplayer.commonapi.events.MirrorLinkNotSupportedEvent;
import com.carconnectivity.mlmediaplayer.commonapi.events.MirrorLinkSessionChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderPlaybackState;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderView;
import com.carconnectivity.mlmediaplayer.mediabrowser.SessionManager;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.DisableEventsEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.MediaButtonClickedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.PlaybackStateChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderDiscoveryFinished;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.TerminateEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.MediaButtonData;
import com.carconnectivity.mlmediaplayer.ui.launcher.LauncherFragment;
import com.carconnectivity.mlmediaplayer.ui.navigator.NavigatorFragment;
import com.carconnectivity.mlmediaplayer.ui.player.MediaPlayerFragment;
import com.carconnectivity.mlmediaplayer.ui.splash.SplashScreenFragment;
import com.carconnectivity.mlmediaplayer.utils.FontOverride;
import com.carconnectivity.mlmediaplayer.utils.UiUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

public class MainActivity extends Activity implements InteractionListener {
    private final static String TAG = MainActivity.class.getCanonicalName();
    private final static String ML_TERMINATE_INTENT = "com.mirrorlink.android.app.TERMINATE";
    private final static String ML_LAUNCH_INTENT = "com.mirrorlink.android.app.LAUNCH";

    private SessionManager mManager;

    private MediaPlayerFragment mPlayerFragment;
    private NavigatorFragment mNavigatorFragment;
    private LauncherFragment mLauncherFragment;
    private SplashScreenFragment mSplashFragment;

    private MirrorLinkConnectionManager mMirrorLinkConnectionManager;
    private boolean mFindProviders;

    private EventBus mBus = EventBus.getDefault();

    private boolean mApplyTransition = true;
    private boolean mTerminateReceived = false;

    private boolean mInstaceStateSaved = false;

    private Timer mTerminationTimer;
    private Dialog mDialog;

    public MirrorLinkApplicationContext getMirrorLinkApplicationContext() {
        return (MirrorLinkApplicationContext) getApplicationContext();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        final int flags
                = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FontOverride.forceRobotoFont(this);

        /* Explicitly check and and handle TERMINATE event: */
        final Intent intent = getIntent();
        mTerminateReceived = intent.getAction().equals(ML_TERMINATE_INTENT);
        final boolean mirrorLinkLaunch = intent.getAction().equals(ML_LAUNCH_INTENT);

        final String rootDirectoryName = getResources().getString(R.string.top_level);

        mLauncherFragment = LauncherFragment.newInstance();
        mNavigatorFragment = NavigatorFragment.newInstance(rootDirectoryName);
        mPlayerFragment = new MediaPlayerFragment(); /* TODO: replace this with newInstance() */
        mSplashFragment = SplashScreenFragment.newInstance();

        if (!mBus.isRegistered(this)) { /* TODO: this should not be required we are unregistering in onDestroy, check & remove */
            mBus.register(this);
        }

        mManager = new SessionManager(this, getPackageManager());
        mManager.findProviders();

        final MirrorLinkApplicationContext mirrorLinkApplicationContext
                = getMirrorLinkApplicationContext();
        mMirrorLinkConnectionManager
                = new MirrorLinkConnectionManager(mirrorLinkApplicationContext, this);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, mSplashFragment, "mSplashFragment")
                .commit();

        mFindProviders = false;
        if (mirrorLinkLaunch == false && mTerminateReceived == false) {
            showNotSupportedDialog();
        }
    }

    private void forceExit() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);

        System.exit(0);
    }

    @SuppressWarnings("unused")
    public void onEvent(PlaybackStateChangedEvent event) {
        if (mTerminateReceived) {
            final ProviderPlaybackState state = event.state;
            if (state.state == PlaybackState.STATE_PLAYING) {
                mBus.postSticky(new TerminateEvent());
            } else if (state.state == PlaybackState.STATE_PAUSED
                    || state.state == PlaybackState.STATE_STOPPED) {
                forceExit();
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderDiscoveryFinished event) {
        if (mTerminateReceived) {
            mTerminationTimer = new Timer();
            mTerminationTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            forceExit();
                        }
                    });
                }
            }, 1000);
        }
    }

    private void showNotSupportedDialog() {
        if (mDialog != null) return;
        if (mTerminateReceived) return;

        final Resources resources = getResources();
        final String appName = resources.getString(R.string.app_name);
        final String rawMessage = resources.getString(R.string.ml_not_connected);
        final String message = String.format(rawMessage, appName);

        mDialog = UiUtilities.showDialog(this, message);
    }

    private void hideNotSupportedDialog() {
        if (mDialog == null) return;

        mDialog.hide();
        mDialog = null;
    }

    @SuppressWarnings("unused")
    public void onEvent(MirrorLinkNotSupportedEvent event) {
        showNotSupportedDialog();
    }

    @SuppressWarnings("unused")
    public void onEvent(MirrorLinkSessionChangedEvent event) {
        if (event.headUnitIsConnected) {
            hideNotSupportedDialog();
        } else {
            showNotSupportedDialog();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mInstaceStateSaved = false;
        if ((getFragmentManager().getBackStackEntryCount() == 0) && !mSplashFragment.isAdded()) {
            showLauncher();
        }
        if (mFindProviders) {
            mManager.refreshProviders();
            mFindProviders = false;
        }
        mManager.tryReconnect();
    }


    @Override
    public void onPause() {
        super.onPause();
        mManager.disconnect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mFindProviders = true;
        mManager.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFindProviders = true;
        mManager.disconnect();
        EventBus.getDefault().post(new DisableEventsEvent());
        mBus.unregister(this);
        mMirrorLinkConnectionManager.disconnectFromApiService();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        mInstaceStateSaved = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onBackPressed() {
        boolean useDefaultBehavior = true;
        final Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null && currentFragment instanceof BackButtonHandler) {
            BackButtonHandler handlerFragment = (BackButtonHandler) currentFragment;
            useDefaultBehavior = handlerFragment.handleBackButtonPress();
        }
        if (mMirrorLinkConnectionManager.isInDriveMode()) {
            useDefaultBehavior = false;
        }

        if (useDefaultBehavior) {
            super.onBackPressed();
        }
    }

    private Fragment reinstantiateFragment(Fragment detachedFragment) {
        if (detachedFragment.isDetached() == false)
            return detachedFragment;

        if (detachedFragment instanceof LauncherFragment) {
            return LauncherFragment.newInstance();
        } else if (detachedFragment instanceof NavigatorFragment) {
            final NavigatorFragment oldNavigator = (NavigatorFragment) detachedFragment;

            final String rootDirectoryName = getResources().getString(R.string.top_level);
            final NavigatorFragment newNavigator = NavigatorFragment.newInstance(rootDirectoryName);

            /* Force state transfer from previous instance to new instance: */
            final Bundle state = new Bundle();
            oldNavigator.saveState(state);
            newNavigator.loadState(state);

            return newNavigator;
        } else if (detachedFragment instanceof MediaPlayerFragment) {
            return new MediaPlayerFragment(); /* TODO: replace this with newInstance() */
        } else if (detachedFragment instanceof SplashScreenFragment) {
            return SplashScreenFragment.newInstance();
        }

        throw new IllegalArgumentException
                ("Failed to create new instance. Unsupported fragment type.");
    }


    private void switchFragment(Fragment fragment) {
        if (mInstaceStateSaved) {
            Log.e(TAG, "Cannot change fragment, onSavedInstanceState was called.");
            return;
        }

        if (fragment.isDetached()) {
            /* This is a workaround for a bug in Android:
             *   https://code.google.com/p/android/issues/detail?id=42601
             */
            fragment = reinstantiateFragment(fragment);
        }
        Fragment currentFragment = getCurrentFragment();
        boolean shouldBeAddedToBackStack
                = !(currentFragment instanceof SplashScreenFragment);
        final FragmentManager manager = getFragmentManager();
        final FragmentTransaction transaction = manager.beginTransaction();

        if (fragment instanceof LauncherFragment) {
            while (manager.getBackStackEntryCount() != 0) {
                manager.popBackStackImmediate();
            }
            shouldBeAddedToBackStack = false;
        }

        applyTransition(transaction, fragment);
        transaction.replace(R.id.container, fragment);
        transaction.attach(fragment);

        if(fragment instanceof MediaPlayerFragment){
            if (currentFragment instanceof LauncherFragment) {
                shouldBeAddedToBackStack=true;
            } else if (currentFragment instanceof NavigatorFragment) {
                shouldBeAddedToBackStack=true;
            }
        }

        if (shouldBeAddedToBackStack)
            transaction.addToBackStack(null);
        transaction.commit();
    }

    public void openLauncher(View caller) {
        if (mLauncherFragment == null)
            mLauncherFragment = LauncherFragment.newInstance();
        switchFragment(mLauncherFragment);
    }

    public void openNavigator(View caller) {
        if (mNavigatorFragment == null) {
            final String rootDirectoryName = getResources().getString(R.string.top_level);
            mNavigatorFragment = NavigatorFragment.newInstance(rootDirectoryName);
        }
        switchFragment(mNavigatorFragment);
    }

    public void openMediaPlayer(View caller) {
        if (mPlayerFragment == null)
            mPlayerFragment = new MediaPlayerFragment();
        switchFragment(mPlayerFragment);
    }

    public ProviderView getNowPlayingProvider() {
        return mManager.getNowPlayingProviderView();
    }

    @Override
    public void showNavigator(boolean startInRootDirectory) {
        if (startInRootDirectory && mNavigatorFragment != null) {
            mNavigatorFragment.clearCrumbs();
        }
        openNavigator(null);
    }

    @Override
    public void showMediaPlayer() {
        openMediaPlayer(null);
    }

    @Override
    public void showLauncher() {
        openLauncher(null);
    }

    private Fragment getCurrentFragment() {
        List<Integer> ids = new ArrayList<>();
        if (mLauncherFragment != null) ids.add(mLauncherFragment.getId());
        if (mNavigatorFragment != null) ids.add(mNavigatorFragment.getId());
        if (mPlayerFragment != null) ids.add(mPlayerFragment.getId());
        if (mSplashFragment != null) ids.add(mSplashFragment.getId());
        for (int id : ids) {
            Fragment fragment = getFragmentManager().findFragmentById(id);
            if (fragment != null && fragment.isVisible()) {
                return fragment;
            }
        }
        return null;
    }

    private void applyTransition(FragmentTransaction transaction, Fragment toFragment) {
        if (mApplyTransition == false) return;

        final Fragment currentFragment = getCurrentFragment();
        final int enterTransition = resolveEnterAnimation(toFragment);
        final int exitTransition = resolveExitAnimation(currentFragment);
        final int popEnterTransition = resolveEnterAnimation(currentFragment);
        final int popExitTransition = resolveExitAnimation(toFragment);

        transaction.setCustomAnimations
                (enterTransition, exitTransition, popEnterTransition, popExitTransition);
    }

    private int resolveEnterAnimation(Fragment to) {
        if (to == mLauncherFragment) {
            return R.xml.transition_slide_left_in;
        } else if (to == mNavigatorFragment) {
            return R.xml.transition_slide_right_in;
        }
        return R.xml.transition_fade_in;
    }

    private int resolveExitAnimation(Fragment from) {
        if (from == mLauncherFragment) {
            return R.xml.transition_slide_left_out;
        } else if (from == mNavigatorFragment) {
            return R.xml.transition_slide_right_out;
        }
        return R.xml.transition_fade_out;
    }

    // Unfortunately we need to resort to this little hack to override
    // Android's default TAB focus navigation for the launcher and the
    // Navigator
    private final Instrumentation mInstrumentation = new Instrumentation();

    private void injectKeyEvent(final int keycode) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                mInstrumentation.sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, keycode));
                mInstrumentation.sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, keycode));
            }
        };
        thread.start();
    }

    // This is a hack that allows to get sane navigation inside ListViews and GridViews using
    // rotary knob rotation
    private boolean handleListFragments(int keycode, KeyEvent event) {
        final View focusedView = getWindow().getDecorView().findFocus();
        if ((focusedView != null)
                && (focusedView instanceof android.widget.AbsListView)
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((getCurrentFragment() instanceof NavigatorFragment) && (focusedView instanceof android.widget.ListView)) {
                final android.widget.ListView listView = (android.widget.ListView) focusedView;
                final com.carconnectivity.mlmediaplayer.ui.navigator.NavigatorListAdapter adapter = (com.carconnectivity.mlmediaplayer.ui.navigator.NavigatorListAdapter) listView.getAdapter();
                final int selectedPosition = listView.getSelectedItemPosition();
                final int itemsCount = listView.getCount();
                boolean paginateUp = false;
                boolean paginateDown = false;
                if (keycode == KeyEvent.KEYCODE_TAB) {
                    if (event.hasNoModifiers()) {
                        if (selectedPosition < (itemsCount - 1)) {
                            injectKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN);
                            return true;
                        } else {
                            paginateDown = true;
                        }
                    } else {
                        if (selectedPosition > 0) {
                            injectKeyEvent(KeyEvent.KEYCODE_DPAD_UP);
                            return true;
                        } else {
                            paginateUp = true;
                        }
                    }
                } else if (keycode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (adapter.usePagination() && (selectedPosition == (itemsCount - 1))) {
                        paginateDown = true;
                    }
                } else if (keycode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (adapter.usePagination() && (selectedPosition == 0)) {
                        paginateUp = true;
                    }
                }
                if (paginateDown) {
                    if (adapter.usePagination()) {
                        final int page = adapter.getCurrentPage();
                        if (page < (adapter.getPagesCount() - 1)) {
                            adapter.goToPage(page + 1);
                            listView.setSelection(0);
                            ((NavigatorFragment) getCurrentFragment()).refreshPaginationController();
                            return true;
                        }
                    }
                } else if (paginateUp) {
                    if (adapter.usePagination()) {
                        final int page = adapter.getCurrentPage();
                        if (page > 0) {
                            adapter.goToPage(page - 1);
                            listView.setSelection(listView.getCount() - 1);
                            ((NavigatorFragment) getCurrentFragment()).refreshPaginationController();
                            return true;
                        }
                    }
                }
            } else {
                if (keycode == KeyEvent.KEYCODE_TAB) {
                    final android.widget.AbsListView absListView = (android.widget.AbsListView) focusedView;
                    final int selectedPosition = absListView.getSelectedItemPosition();
                    final int itemsCount = absListView.getCount();
                    if (event.hasNoModifiers()) {
                        if (selectedPosition < (itemsCount - 1)) {
                            absListView.setSelection(selectedPosition + 1);
                            return true;
                        }
                    } else {
                        if (selectedPosition > 0) {
                            absListView.setSelection(selectedPosition - 1);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (isDiagonalShift(keycode)) {
            return true;
        }

        final MediaButtonData.Type buttonType = buttonTypeFromKey(keycode);
        if (buttonType != null) {
            final ProviderView provider = mManager.getNowPlayingProviderView();
            final MediaButtonData mediaButtonData
                    = new MediaButtonData(provider, buttonType, null, null, null);
            mBus.post(new MediaButtonClickedEvent(mediaButtonData));
            return true;
        }

        if (handleListFragments(keycode, event)) {
            return true;
        }

        return super.onKeyDown(keycode, event);
    }

    private static boolean isDiagonalShift(int keycode) {
        switch (keycode) {
            case 188: // up-right diagonal
            case 189: // up-left diagonal
            case 190: // down-right diagonal
            case 191: // down-left diagonal
            case 102: // rotate X clockwise
            case 103: // rotate Y anti-clockwise
            case 104: // rotate X clockwise
            case 105: // rotate Y anti-clockwise
                return true;
            default:
                return false;
        }
    }

    private MediaButtonData.Type buttonTypeFromKey(int keycode) {
        MediaButtonData.Type buttonType = null;
        switch (keycode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                buttonType = MediaButtonData.Type.PLAY;
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                buttonType = MediaButtonData.Type.PAUSE;
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                buttonType = MediaButtonData.Type.SKIP_TO_NEXT;
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                buttonType = MediaButtonData.Type.SKIP_TO_PREVIOUS;
                break;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                buttonType = MediaButtonData.Type.FAST_FORWARD;
                break;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                buttonType = MediaButtonData.Type.REWIND;
                break;
        }
        return buttonType;
    }
}
