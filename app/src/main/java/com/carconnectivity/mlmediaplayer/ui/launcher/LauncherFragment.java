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

package com.carconnectivity.mlmediaplayer.ui.launcher;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.commonapi.events.DriveModeStatusChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderView;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.DisableEventsEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.NowPlayingProviderChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderConnectedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderDiscoveredEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.StartBrowsingEvent;
import com.carconnectivity.mlmediaplayer.ui.InteractionListener;
import com.carconnectivity.mlmediaplayer.ui.MainActivity;
import com.carconnectivity.mlmediaplayer.utils.UiUtilities;
import com.carconnectivity.mlmediaplayer.utils.pagination.PaginationController;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class LauncherFragment extends Fragment {
    private static final String TAG = LauncherFragment.class.getSimpleName();
    private boolean mUsePagination = false;
    private PaginationController mPaginationController;

    private LauncherGridAdapter mActiveAdapter;
    private LauncherGridAdapter mInactiveAdapter;
    private WeakReference<InteractionListener> mListener;

    private List<ProviderView> mListProviders;
    private GridView mActiveGrid;
    private GridView mInactiveGrid;
    private ProviderView mNowPlayingProvider;

    private TextView mSelectAppHint;
    private TextView mNoAppsWarning;
    private boolean mIgnoreGoToPlayerOnConnection;
    private boolean mInDriveMode;

    private View.OnFocusChangeListener mFocusListener;
    private ImageButton mBackButton;

    public static LauncherFragment newInstance() {
        LauncherFragment fragment = new LauncherFragment();
        fragment.mListProviders = new ArrayList<>();
        fragment.mIgnoreGoToPlayerOnConnection = true;

        EventBus.getDefault().register(fragment);

        return fragment;
    }

    public void enableIgnoreGoToPlayerOnConnection(boolean enabled) {
        mIgnoreGoToPlayerOnConnection = enabled;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void clearList() {
        mListProviders.clear();
        if (mActiveAdapter != null && mInactiveAdapter != null) {
            mActiveAdapter.removeItems();
            mInactiveAdapter.removeItems();
        }
    }

    public LauncherFragment() {
        // Required empty public constructor
    }

    @SuppressWarnings("unused")
    public void onEvent(DriveModeStatusChangedEvent event) {
        enableDriveMode(event.isDriveModeActive);
    }

    @SuppressWarnings("unused")
    public void onEvent(DisableEventsEvent event) {
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderDiscoveredEvent event) {
        Log.d(TAG, "Received ProviderDiscoveredEvent: " + event.provider.getUniqueName() + " " + event.isPlaying);
        final ProviderView provider = event.provider;
        mListProviders.add(provider);
        if (mActiveAdapter != null && provider.canConnect()) {
            mActiveAdapter.addItem(provider);
        } else if (mInactiveAdapter != null) {
            mInactiveAdapter.addItem(provider);
        }

        if (mActiveAdapter != null) {
            handleWarningVisibility(mActiveAdapter.getCount());
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderConnectedEvent event) {
        if (event.provider != null) {
            if (mIgnoreGoToPlayerOnConnection == false && event.showPlayer && mListener != null) {
                mListener.get().showMediaPlayer();
            }
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(NowPlayingProviderChangedEvent event) {
        mNowPlayingProvider = event.provider;
        initializeNowPlayingProviderDisplay(getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.c4_fragment_launcher, container, false);

        mFocusListener = UiUtilities.defaultOnFocusChangeListener((MainActivity) getActivity());

        mActiveGrid = (GridView) root.findViewById(R.id.grid_active);
        mInactiveGrid = (GridView) root.findViewById(R.id.grid_inactive);

        mSelectAppHint = (TextView) root.findViewById(R.id.text_select_hint);
        mNoAppsWarning = (TextView) root.findViewById(R.id.no_auto_apps_warning);

        initializeScrollView(root);
        initializeNowPlayingProviderDisplay(root);
        initializeBackButton(root);
        initializeAdapters(root);
        mPaginationController.initializePagination(root, mFocusListener);

        setGridAdapter(mActiveGrid, mActiveAdapter);
        setGridAdapter(mInactiveGrid, mInactiveAdapter);
        mInactiveGrid.setFocusable(false);
        //mInactiveGrid.setVisibility(mUsePagination ? View.GONE : View.VISIBLE);
        mInactiveGrid.setVisibility(View.GONE);

        if (mNowPlayingProvider != null) {
            final int color = mNowPlayingProvider.getDisplayInfo().colorAccent;
            final Drawable selector = mActiveGrid.getSelector();
            selector.setTintMode(PorterDuff.Mode.MULTIPLY);
            selector.setTint(color);

            ScrollView scrollView = (ScrollView) root.findViewById(R.id.scrollview);
            UiUtilities.setScrollBarTint(scrollView, getResources(), color);

            mPaginationController.changeActiveColor(color);
        }

        mActiveGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                EventBus.getDefault().postSticky(new ProviderConnectedEvent(null, false, false));
                ProviderView providerView = (ProviderView) mActiveAdapter.getItem(i);

                final boolean noCurrentProvider = mNowPlayingProvider == null;
                final boolean hasSomethingToPlay
                        =  providerView.getCurrentMetadata() != null
                        && providerView.getCurrentMetadata().isTitleEmpty() == false;
                final boolean isNowPlaying
                        = mNowPlayingProvider != null
                        && mNowPlayingProvider.hasSameNameAs(providerView);
                final boolean showPlayer = (noCurrentProvider && hasSomethingToPlay) || isNowPlaying;
                onProviderSelected(providerView, showPlayer);
            }
        });

        return root;
    }

    private void enableDriveMode(boolean enable) {
        enablePagination(enable);
        mInDriveMode = enable;
        initializeBackButton(getView());
    }

    private void onProviderSelected(ProviderView providerView, boolean showPlayer) {
        if (providerView != null && providerView.canConnect()) {
            if (mListener != null) {
                if (showPlayer) {
                    mListener.get().showMediaPlayer();
                } else {
                    mListener.get().showNavigator(true);
                }
            }
            EventBus.getDefault().post(new StartBrowsingEvent(providerView));
        }
    }

    private void initializeBackButton(View root) {
        if (root == null) return;

        mBackButton = (ImageButton) root.findViewById(R.id.launcher_back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getFragmentManager().getBackStackEntryCount() == 0) getActivity().finish();
                getFragmentManager().popBackStack();
            }
        });
        mBackButton.setOnFocusChangeListener(mFocusListener);

        final boolean backInvisible
                = mInDriveMode && getFragmentManager().getBackStackEntryCount() == 0;
        UiUtilities.setVisibility(mBackButton, backInvisible == false);
    }

    private void initializeScrollView(View root) {
        if (root == null) return;

        final ScrollView scrollView = (ScrollView) root.findViewById(R.id.scrollview);
        UiUtilities.disableInertialScrolling(scrollView, getActivity());
    }

    private void initializeNowPlayingProviderDisplay(View root) {
        if (root == null) return;

        String providerName = "";
        Drawable providerIcon = null;

        Button providerButton = (Button) root.findViewById(R.id.buttonLauncher);
        if (mNowPlayingProvider != null) {
            ProviderView.ProviderDisplayInfo info = mNowPlayingProvider.getDisplayInfo();
            providerName = getResources().getString(R.string.now_playing);
            providerButton.setVisibility(View.VISIBLE);
            providerIcon = info.notificationDrawable != null ? info.notificationDrawable : info.icon;
            showSelectAppHint(false);
        } else {
            providerButton.setVisibility(View.INVISIBLE);
            showSelectAppHint(true);
        }
        providerButton.setText(providerName);

        providerButton.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, providerIcon, null);
        providerButton.setCompoundDrawablePadding(50);
        providerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.get().showMediaPlayer();
                }
            }
        });
        providerButton.invalidate();
        providerButton.setOnFocusChangeListener(mFocusListener);
    }

    private void showSelectAppHint(boolean visible) {
        if (mSelectAppHint == null) return;
        UiUtilities.setVisibility(mSelectAppHint, visible);
    }

    private void enablePagination(boolean enabled) {
        mUsePagination = enabled;
        initializeAdapters(getView());
        if (mInactiveGrid != null) {
            //mInactiveGrid.setVisibility(mUsePagination ? View.GONE : View.VISIBLE);
            mInactiveGrid.setVisibility(View.GONE);
        }
    }

    private void handleWarningVisibility(int activeCount) {
        if (mNoAppsWarning == null) return;
        UiUtilities.setVisibility(mNoAppsWarning, activeCount <= 0);
    }

    private void initializeAdapters(View root) {
        ArrayList<ProviderView> activeProviders = new ArrayList<>();
        ArrayList<ProviderView> inactiveProviders = new ArrayList<>();
        if (mListProviders != null) {
            for (ProviderView provider : mListProviders) {
                if (provider.canConnect()) {
                    activeProviders.add(provider);
                }
                else {
                    inactiveProviders.add(provider);
                }
            }
        }
        mActiveAdapter = new LauncherGridAdapter(this, activeProviders, mUsePagination);
        mInactiveAdapter = new LauncherGridAdapter(this, inactiveProviders, mUsePagination);

        if (mActiveGrid != null) {
            setGridAdapter(mActiveGrid, mActiveAdapter);
        }

        mPaginationController = new PaginationController(mActiveAdapter, mUsePagination);
        mPaginationController.initializePagination(root, mFocusListener);

        mActiveAdapter.notifyDataSetChanged();
        handleWarningVisibility(activeProviders.size());
    }

    private static void setGridAdapter(GridView grid, LauncherGridAdapter adapter) {
        grid.setAdapter(adapter);
        adapter.setOwner(grid);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = new WeakReference<InteractionListener>((InteractionListener) activity);
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
