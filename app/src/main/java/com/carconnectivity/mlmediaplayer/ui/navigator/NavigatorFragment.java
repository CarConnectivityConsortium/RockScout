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

package com.carconnectivity.mlmediaplayer.ui.navigator;

import android.app.Fragment;
import android.content.ComponentName;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.commonapi.events.MirrorLinkSessionChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.MediaItemView;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderViewActive;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.BrowseDirectoryEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.CurrentlyBrowsedProviderChanged;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.DisconnectFromProviderEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.NowPlayingProviderChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.PlayMediaItemEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderBrowseErrorEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderBrowseSuccessfulEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderConnectErrorEvent;
import com.carconnectivity.mlmediaplayer.ui.BackButtonHandler;
import com.carconnectivity.mlmediaplayer.ui.MainActivity;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;
import com.carconnectivity.mlmediaplayer.utils.UiUtilities;
import com.carconnectivity.mlmediaplayer.utils.breadcrumbs.BreadCrumbs;
import com.carconnectivity.mlmediaplayer.utils.breadcrumbs.NavigatorLevel;
import com.carconnectivity.mlmediaplayer.utils.pagination.PaginationController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class NavigatorFragment extends Fragment implements BackButtonHandler {

    private static final String TAG = NavigatorFragment.class.getSimpleName();
    private static final String PROVIDER_NAME_KEY
            = NavigatorFragment.class.getSimpleName() + ".provider_name";
    private static final String BREAD_CRUMBS_KEY
            = NavigatorFragment.class.getSimpleName() + ".bread_crumbs";
    private static final long SHOW_MISSING_DELAY = TimeUnit.SECONDS.toMillis(20);
    private State mState;

    private ProviderViewActive mNowPlayingProvider;
    private ProviderViewActive mCurrentlyBrowsedProvider;
    private String mRestoredProviderName;

    private BreadCrumbs mCrumbs;

    private PaginationController mPaginationController;
    private boolean mUsePagination = false;

    private ListView mList;
    private NavigatorListAdapter mAdapter;
    private List<MediaItemView> mCurrentProviderItems;

    private TextView mDirectoryNameLabel;
    private TextView mNoResultsLabel;
    private ProgressBar mWaitIndicator;
    private final CountDownTimer mWaitForResultsTimer
            = new CountDownTimer(SHOW_MISSING_DELAY, SHOW_MISSING_DELAY) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            changeState(State.FAILED);
        }
    };
    private View.OnFocusChangeListener mFocusListener;
    private ProviderBrowseSuccessfulEvent mLastProviderBrowseSuccessfulEvent;

    public NavigatorFragment() {
        // Required empty public constructor
    }

    public static NavigatorFragment newInstance(String topLevelName) {
        NavigatorFragment fragment = new NavigatorFragment();
        fragment.mCrumbs = new BreadCrumbs(topLevelName);
        fragment.mState = State.CREATED;

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    private static void setListAdapter(ListView list, NavigatorListAdapter adapter) {
        list.setAdapter(adapter);
        adapter.setOwner(list);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(BrowseDirectoryEvent event) {
        changeState(State.LOADING);
    }

    @SuppressWarnings("unused")
    public void onEvent(DisconnectFromProviderEvent event) {
        clearCrumbs();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(MirrorLinkSessionChangedEvent event) {
        enablePagination(event.headUnitIsConnected);
    }

    @SuppressWarnings("unused")
    public void onEvent(CurrentlyBrowsedProviderChanged event) {
        changeBrowsedProvider(event.provider);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ProviderBrowseSuccessfulEvent event) {
        final String currentDirectoryId = mCrumbs.getTopItem().id;
        if (currentDirectoryId != null && currentDirectoryId.equals(event.parentId) == false) {
            Log.d(TAG, "Got browse response with unexpected parent id.");
            return;
        }

        if (mAdapter != null) {
            mAdapter.setItems(event.items);
            mPaginationController.initializePagination(getView(), mFocusListener);
        }

        if (event.items.size() == 0) {
            changeState(State.FAILED);
        } else {
            mLastProviderBrowseSuccessfulEvent = event;
            changeState(State.LOADED);
        }

        mCurrentProviderItems = new ArrayList<>(event.items);
    }

    public void refreshPaginationController() {
        Log.d(TAG, "refreshPaginationController");
        mPaginationController.setNumbers();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void changeBrowsedProvider(ProviderViewActive provider) {
        Log.d(TAG, "changeBrowsedProvider: provider=" + (provider != null ? provider.toString() : "null"));
        mCurrentlyBrowsedProvider = provider;
        /* if the same name was loaded form the saved instance
         * do not reject current state of the bread crumbs
         */
        final String newName = provider.getUniqueName().toString();
        if (newName.equals(mRestoredProviderName) == false) {
            clearCrumbs();
        }

        mRestoredProviderName = newName;
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ProviderBrowseErrorEvent event) {
        changeState(State.FAILED);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ProviderConnectErrorEvent event) {
        changeState(State.FAILED);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NowPlayingProviderChangedEvent event) {
        mNowPlayingProvider = event.provider;

        if (event.provider == null) {
            mNowPlayingProvider = mCurrentlyBrowsedProvider;
        }
        initializeNowPlayingProviderDisplay(getView());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            loadState(savedInstanceState);
        }
        mFocusListener = UiUtilities.defaultOnFocusChangeListener((MainActivity) getActivity());
        RsEventBus.registerSticky(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        RsEventBus.unregister(this);
    }

    private void changeState(State state) {
        Log.d(TAG, "changeState: state" + (state != null ? state.name() : "null"));

        switch (state) {
            case LOADING:
                changeToLoading();
                break;
            case LOADED:
                changeToLoaded();
                break;
            case FAILED:
            default:
                changeToFailed();
                break;
        }
        mState = state;
    }

    private void changeToLoading() {
        Log.d(TAG, "changeToLoading");
        startTimer();
        enableWaitIndicator(true);
        enableFailurePrompt(false);
        enableItemsList(false);
        if (mPaginationController != null) {
            mPaginationController.setVisibility(false);
        }
    }

    private void changeToLoaded() {
        Log.d(TAG, "changeToLoaded");
        stopTimer();
        enableWaitIndicator(false);
        enableFailurePrompt(false);
        enableItemsList(true);
        if (mPaginationController != null) {
            mPaginationController.setVisibility(true);
        }
    }

    private void changeToFailed() {
        Log.d(TAG, "changeToFailed");
        stopTimer();
        enableWaitIndicator(false);
        enableFailurePrompt(true);
        enableItemsList(false);
        if (mPaginationController != null) {
            mPaginationController.setVisibility(false);
        }
    }

    public void clearCrumbs() {
        Log.d(TAG, "clearCrumbs");
        mLastProviderBrowseSuccessfulEvent = null;
        mCrumbs.reset();
    }

    private void enablePagination(boolean enablePagination) {
        Log.d(TAG, "enablePagination: enablePagination=" + enablePagination);
        mUsePagination = enablePagination;
        initializeAdapter();
    }

    public void saveState(Bundle bundle) {
        if (bundle == null) {
            throw new IllegalArgumentException("Bundle cannot be null.");
        }
        if (mCrumbs != null) {
            final String json = mCrumbs.toJson();
            final String providerName
                    = mCurrentlyBrowsedProvider == null
                    ? null
                    : mCurrentlyBrowsedProvider.getUniqueName().toString();

            bundle.putString(BREAD_CRUMBS_KEY, json);
            bundle.putString(PROVIDER_NAME_KEY, providerName);
        }
    }

    public void loadState(Bundle bundle) {
        if (bundle == null) {
            throw new IllegalArgumentException("Bundle cannot be null.");
        }

        if (bundle.containsKey(BREAD_CRUMBS_KEY)) {
            final String json = bundle.getString(BREAD_CRUMBS_KEY);
            mCrumbs = BreadCrumbs.fromJson(json);
            mLastProviderBrowseSuccessfulEvent = null;
            mRestoredProviderName = bundle.getString(PROVIDER_NAME_KEY);
        }
    }

    private void browseCurrentDirectory() {
        Log.d(TAG, "browseCurrentDirectory");
        final NavigatorLevel currentLevel = mCrumbs.getTopItem();
        browseDirectory(currentLevel.id);
        mDirectoryNameLabel.setText(currentLevel.displayName);
    }

    private void onGoingBack() {
        Log.d(TAG, "onGoingBack");
        if (mCrumbs.canGoBack()) {
            popLevel();
        } else {
            ((MainActivity) getActivity()).openLauncher(null);
        }
    }

    private void pushNewLevel(String displayName, String id) {
        Log.d(TAG, "pushNewLevel");
        mLastProviderBrowseSuccessfulEvent = null;
        mCrumbs.push(displayName, id);
        mDirectoryNameLabel.setText(displayName);
        browseDirectory(id);
    }

    private void popLevel() {
        Log.d(TAG, "popLevel");
        mLastProviderBrowseSuccessfulEvent = null;
        mCrumbs.goBack();
        mDirectoryNameLabel.setText(mCrumbs.getTopItem().displayName);
        browseDirectory(mCrumbs.getTopItem().id);
    }

    private void browseDirectory(String directoryId) {
        Log.d(TAG, "browseDirectory");
        final ComponentName providerName = mCurrentlyBrowsedProvider.getUniqueName();
        RsEventBus.post(new BrowseDirectoryEvent(providerName, directoryId));
        changeState(State.LOADING);
    }

    @Override
    public boolean handleBackButtonPress() {
        onGoingBack();
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        final View root = inflater.inflate(R.layout.c4_fragment_navigator, container, false);
        mDirectoryNameLabel = (TextView) root.findViewById(R.id.parent_directory);
        mNoResultsLabel = (TextView) root.findViewById(R.id.no_results_notification);

        if (mAdapter == null) initializeAdapter();

        if (savedInstanceState != null) {
            loadState(savedInstanceState);
        }

        final ImageView mBackButton = (ImageView) root.findViewById(R.id.navigator_back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onGoingBack();
            }
        });
        mBackButton.setOnFocusChangeListener(mFocusListener);

        initializeNowPlayingProviderDisplay(root);

        mList = (ListView) root.findViewById(R.id.navigator_list);
        mList.setFocusable(true);
        mList.setItemsCanFocus(true);
        mList.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        setListAdapter(mList, mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final MediaItemView mediaItem = (MediaItemView) mAdapter.getItem(i);
                if (mediaItem.isBrowsable()) {
                    pushNewLevel(mediaItem.getDisplayLabel(), mediaItem.getId());
                } else if (mediaItem.isPlayable()) {
                    playMediaItem(mediaItem.getId(), mediaItem.getExtras());
                }
            }
        });
        UiUtilities.disableInertialScrollingOnList(mList, getActivity());

        mWaitIndicator = (ProgressBar) root.findViewById(R.id.waitIndicator);
        mWaitIndicator.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        mPaginationController.initializePagination(root, mFocusListener);

        if (mNowPlayingProvider != null) {
            final int color = mNowPlayingProvider.getColorAccent();
            final Drawable selector = mList.getSelector();
            selector.setTint(color);
            selector.setTintMode(PorterDuff.Mode.MULTIPLY);

            UiUtilities.setScrollBarTint(mList, getResources(), color);

            mPaginationController.changeActiveColor(color);
        }

        changeState(State.LOADING);
        if (mLastProviderBrowseSuccessfulEvent != null) {
            onEventMainThread(mLastProviderBrowseSuccessfulEvent);
        } else {
            browseCurrentDirectory();
        }

        return root;
    }

    private void playMediaItem(String mediaId, Bundle bundle) {
        final PlayMediaItemEvent event
                = new PlayMediaItemEvent(mCurrentlyBrowsedProvider, mediaId, bundle);
        RsEventBus.post(event);
        ((MainActivity) getActivity()).openMediaPlayer(null);
    }

    private void initializeAdapter() {
        mAdapter = new NavigatorListAdapter(this, mUsePagination);
        mPaginationController = new PaginationController(mAdapter, mUsePagination);
        if (mCurrentProviderItems != null && mCurrentProviderItems.size() != 0) {
            mAdapter.setItems(mCurrentProviderItems);
        }

        if (mList != null) {
            setListAdapter(mList, mAdapter);
        }

        final View root = getView();
        if (root != null) mPaginationController.initializePagination(root, mFocusListener);
    }

    private void initializeNowPlayingProviderDisplay(final View root) {
        if (root == null) return;

        Drawable providerIcon = null;
        View.OnClickListener listener = null;

        if (mNowPlayingProvider != null) {
            providerIcon = mNowPlayingProvider.getNotificationDrawable() != null ?
                    mNowPlayingProvider.getNotificationDrawable() : mNowPlayingProvider.getIconDrawable();
            listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) getActivity()).openMediaPlayer(root);
                }
            };
        }

        Button providerButton = (Button) root.findViewById(R.id.buttonLauncher);
        providerButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, providerIcon, null);
        providerButton.setCompoundDrawablePadding(50);
        providerButton.setText(R.string.now_playing);
        providerButton.setOnClickListener(listener);
        providerButton.setOnFocusChangeListener(mFocusListener);

        /* update list highlight to match current provider */
        if (mNowPlayingProvider != null && mList != null) {
            final Drawable selector = mList.getSelector();
            selector.setTint(mNowPlayingProvider.getColorAccent());
            selector.setTintMode(PorterDuff.Mode.MULTIPLY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        saveState(outState);
    }

    private void enableItemsList(boolean enable) {
        if (mList != null) {
            mList.setEnabled(enable);
            if (enable == false) {
                mAdapter.clear();
            }
        }
    }

    private void enableFailurePrompt(boolean enable) {
        if (mNoResultsLabel != null)
            UiUtilities.setVisibility(mNoResultsLabel, enable);
    }

    private void enableWaitIndicator(boolean enable) {
        if (mWaitIndicator != null)
            UiUtilities.setVisibility(mWaitIndicator, enable);
    }

    private void startTimer() {
        mWaitForResultsTimer.cancel();
        mWaitForResultsTimer.start();
    }

    private void stopTimer() {
        mWaitForResultsTimer.cancel();
    }

    /**
     * Think of this fragment as of simple state machine, each visual change
     * should be tied to changing the state.
     */
    private enum State {
        CREATED, LOADING, LOADED, FAILED
    }
}
