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
import android.app.FragmentManager;
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
import com.carconnectivity.mlmediaplayer.commonapi.events.DriveModeStatusChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.MediaItemView;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderView;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.BrowseDirectoryEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.CurrentlyBrowsedProviderChanged;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.DisconnectFromCurrentProviderEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.NowPlayingProviderChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.PlayMediaItemEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderBrowseCancelEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderBrowseErrorEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderConnectErrorEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderBrowseSuccessfulEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderConnectedEvent;
import com.carconnectivity.mlmediaplayer.ui.BackButtonHandler;
import com.carconnectivity.mlmediaplayer.ui.MainActivity;
import com.carconnectivity.mlmediaplayer.utils.UiUtilities;
import com.carconnectivity.mlmediaplayer.utils.breadcrumbs.BreadCrumbs;
import com.carconnectivity.mlmediaplayer.utils.breadcrumbs.NavigatorLevel;
import com.carconnectivity.mlmediaplayer.utils.pagination.PaginationController;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public final class NavigatorFragment extends Fragment implements BackButtonHandler {
    private static final String TAG = NavigatorFragment.class.getSimpleName();
    private static final String PROVIDER_NAME_KEY
            = NavigatorFragment.class.getSimpleName() + ".provider_name";
    private static final String BREAD_CRUMBS_KEY
            = NavigatorFragment.class.getSimpleName() + ".bread_crumbs";

    /** Think of this fragment as of simple state machine, each visual change
     * should be tied to changing the state. */
    private enum State {
        CREATED, LOADING, LOADED, FAILED
    }
    private State mState;

    private ProviderView mNowPlayingProvider;
    private ProviderView mCurrentlyBrowsedProvider;
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

    private View.OnFocusChangeListener mFocusListener;

    public static NavigatorFragment newInstance(String topLevelName) {
        NavigatorFragment fragment = new NavigatorFragment();
        fragment.mCrumbs = new BreadCrumbs(topLevelName);
        fragment.mState = State.CREATED;

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public NavigatorFragment() {
        // Required empty public constructor
    }

    @SuppressWarnings("unused")
    public void onEvent(BrowseDirectoryEvent event) {
        Log.d(TAG, "handle BrowseDirectoryEvent");
        changeState(State.LOADING);
    }

    @SuppressWarnings("unused")
    public void onEvent(DisconnectFromCurrentProviderEvent event) {
        Log.d(TAG, "handle DisconnectFromCurrentProviderEvent");
        clearCrumbs();
    }

    @SuppressWarnings("unused")
    public void onEvent(DriveModeStatusChangedEvent event) {
        enablePagination(event.isDriveModeActive);
        initializeAdapter();
    }

    @SuppressWarnings("unused")
    public void onEvent(CurrentlyBrowsedProviderChanged event) {
        changeBrowsedProvider(event.provider);
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderBrowseSuccessfulEvent event) {
        Log.e(TAG, "handle ProviderBrowseSuccessfulEvent: " + event.toString());

        final String currentDirectoryId = mCrumbs.getTopItem().id;
        if (currentDirectoryId != null && currentDirectoryId.equals(event.parentId) == false) {
            Log.d(TAG, "Got browse response with unexpected parent id.");
            return;
        }

        if (event.items.size() == 0) {
            changeState(State.FAILED);
        } else {
            changeState(State.LOADED);
        }

        if (mAdapter != null) {
            mAdapter.setItems(event.items);
            mPaginationController.initializePagination(getView(), mFocusListener);
        }
        mCurrentProviderItems = new ArrayList<>(event.items);
    }

    public void refreshPaginationController() {
        mPaginationController.setNumbers();
    }

    private void changeBrowsedProvider(ProviderView provider) {
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
    public void onEvent(ProviderBrowseCancelEvent event) {
        Log.d(TAG, "handle ProviderBrowseCancel");
        ((MainActivity) getActivity()).openLauncher(null);
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderBrowseErrorEvent event) {
        Log.d(TAG, "handle ProviderBrowseErrorEvent: " + event.toString());
        changeState(State.FAILED);
    }
 
    @SuppressWarnings("unused")
    public void onEvent(ProviderConnectErrorEvent event) {
        Log.d(TAG, "handle ProviderConnectErrorEvent: " + event.toString());
        changeState(State.FAILED);
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderConnectedEvent event) {
        Log.d(TAG, "handle ProviderConnectedEvent");
        if (event.provider == null) return;
        mCurrentProviderItems = null;
    }

    @SuppressWarnings("unused")
    public void onEvent(NowPlayingProviderChangedEvent event) {
        mNowPlayingProvider = event.provider;
        initializeNowPlayingProviderDisplay(getView());

        if(event.provider == null) {
            ((MainActivity) getActivity()).openLauncher(null);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            loadState(savedInstanceState);
        }
        mFocusListener = UiUtilities.defaultOnFocusChangeListener((MainActivity) getActivity());
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void changeState(State state) {
        if (state == mState)
            return;

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
        startTimer();
        enableWaitIndicator(true);
        enableFailurePrompt(false);
        enableItemsList(false);
    }

    private void changeToLoaded() {
        stopTimer();
        enableWaitIndicator(false);
        enableFailurePrompt(false);
        enableItemsList(true);
    }

    private void changeToFailed() {
        stopTimer();
        enableWaitIndicator(false);
        enableFailurePrompt(true);
        enableItemsList(false);
    }

    public void clearCrumbs() {
        mCrumbs.reset();
    }

    private void enablePagination(boolean enablePagination) {
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
                    : mCurrentlyBrowsedProvider.getUniqueName().toString()
                    ;

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
            mRestoredProviderName = bundle.getString(PROVIDER_NAME_KEY);
        }
    }

    private void browseCurrentDirectory() {
        final NavigatorLevel currentLevel = mCrumbs.getTopItem();
        browseDirectory(currentLevel.id);
        mDirectoryNameLabel.setText(currentLevel.displayName);
    }

    private void onGoingBack() {
        if (mCrumbs.canGoBack()) {
            popLevel();
        } else {
            final FragmentManager manager = getFragmentManager();
            if (manager != null) {
                manager.popBackStack();
            }
        }
    }

    private void pushNewLevel(String displayName, String id) {
        mCrumbs.push(displayName, id);
        mDirectoryNameLabel.setText(displayName);
        browseDirectory(id);
    }

    private void popLevel() {
        mCrumbs.goBack();
        mDirectoryNameLabel.setText(mCrumbs.getTopItem().displayName);
        browseDirectory(mCrumbs.getTopItem().id);
    }

    private void browseDirectory(String directoryId) {
        final ComponentName providerName = mCurrentlyBrowsedProvider.getUniqueName();
        EventBus.getDefault().post(new BrowseDirectoryEvent(providerName, directoryId));
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
            final int color = mNowPlayingProvider.getDisplayInfo().colorAccent;
            final Drawable selector = mList.getSelector();
            selector.setTint(color);
            selector.setTintMode(PorterDuff.Mode.MULTIPLY);

            UiUtilities.setScrollBarTint(mList, getResources(), color);

            mPaginationController.changeActiveColor(color);
        }

        changeState(State.LOADING);
        browseCurrentDirectory();

        return root;
    }

    private static void setListAdapter(ListView list, NavigatorListAdapter adapter) {
        list.setAdapter(adapter);
        adapter.setOwner(list);
    }

    private void playMediaItem(String mediaId, Bundle bundle) {
        final PlayMediaItemEvent event
                = new PlayMediaItemEvent(mCurrentlyBrowsedProvider, mediaId, bundle);
        EventBus.getDefault().post(event);
        /* todo: define interface or send event */
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
            ProviderView.ProviderDisplayInfo info = mNowPlayingProvider.getDisplayInfo();
            providerIcon = info.notificationDrawable != null ? info.notificationDrawable : info.icon;
            listener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /* todo: define interface or send event */
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
            selector.setTint(mNowPlayingProvider.getDisplayInfo().colorAccent);
            selector.setTintMode(PorterDuff.Mode.MULTIPLY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.e(TAG, "on save instance state");
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

    private static final int SHOW_MISSING_DELAY = 5000; /* ms */
    private final CountDownTimer mWaitForResultsTimer
            = new CountDownTimer(SHOW_MISSING_DELAY, SHOW_MISSING_DELAY) {

        @Override public void onTick(long millisUntilFinished) { }

        @Override
        public void onFinish() {
            changeState(State.FAILED);
        }
    };

    private void startTimer() {
        mWaitForResultsTimer.cancel();
        mWaitForResultsTimer.start();
    }

    private void stopTimer() {
        mWaitForResultsTimer.cancel();
    }
}
