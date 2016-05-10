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
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.carconnectivity.mlmediaplayer.commonapi.events.MirrorLinkSessionChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderToDownloadView;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderView;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.DisableEventsEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.NowPlayingProviderChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderConnectedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderDiscoveredEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.ProviderToDownloadDiscoveredEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.StartBrowsingEvent;
import com.carconnectivity.mlmediaplayer.ui.InteractionListener;
import com.carconnectivity.mlmediaplayer.ui.MainActivity;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;
import com.carconnectivity.mlmediaplayer.utils.UiUtilities;
import com.carconnectivity.mlmediaplayer.utils.pagination.PaginationController;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class LauncherFragment extends Fragment {
    private static final String TAG = LauncherFragment.class.getSimpleName();
    private boolean mUsePagination = false;
    private PaginationController mPaginationController;

    private LauncherProviderGridAdapter mProviderAdapter;
    private LauncherProviderToDownloadGridAdapter mProviderToDownloadAdapter;
    private WeakReference<InteractionListener> mListener;

    private List<ProviderView> mListProviders;
    private List<ProviderToDownloadView> mListProvidersToDownload;
    private GridView mProviderGrid;
    private GridView mProviderToDownloadGrid;
    private ProviderView mNowPlayingProvider;

    private TextView mSelectAppHint;
    private TextView mNoAppsWarning;
    private boolean mIgnoreGoToPlayerOnConnection;
    private boolean mInDriveMode;
    private boolean mHeadUnitIsConnected;

    private View.OnFocusChangeListener mFocusListener;
    private ImageButton mBackButton;

    public static LauncherFragment newInstance() {
        LauncherFragment fragment = new LauncherFragment();
        fragment.mListProviders = new ArrayList<>();
        fragment.mListProvidersToDownload = new ArrayList<>();
        fragment.mIgnoreGoToPlayerOnConnection = true;
        fragment.mHeadUnitIsConnected = false;

        RsEventBus.register(fragment);

        return fragment;
    }

    public void enableIgnoreGoToPlayerOnConnection(boolean enabled) {
        mIgnoreGoToPlayerOnConnection = enabled;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RsEventBus.unregister(this);
    }

    public void clearList() {
        mListProviders.clear();
        mListProvidersToDownload.clear();
        if (mProviderAdapter != null && mProviderToDownloadAdapter != null) {
            mProviderAdapter.removeItems();
            mProviderToDownloadAdapter.removeItems();
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
        if (mProviderAdapter != null && provider.canConnect()) {
            mProviderAdapter.addItem(provider);
        }

        if (mProviderAdapter != null) {
            handleGridsVisibility(mProviderAdapter.getCount());
        }

        if (event.isPlaying) {
            RsEventBus.postSticky(new ProviderConnectedEvent(provider, true, false));
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(ProviderToDownloadDiscoveredEvent event) {
        Log.d(TAG, "Received ProviderToDownloadDiscoveredEvent: " + event.provider.getUniqueName());
        final ProviderToDownloadView provider = event.provider;
        mListProvidersToDownload.add(provider);
        if (mProviderToDownloadAdapter != null) {
            mProviderToDownloadAdapter.addItem(provider);
        }

        if (mProviderAdapter != null) {
            handleGridsVisibility(mProviderAdapter.getCount());
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

    @SuppressWarnings("unused")
    public void onEvent(MirrorLinkSessionChangedEvent event) {
        if (event.headUnitIsConnected) {
            mHeadUnitIsConnected = true;
        } else {
            mHeadUnitIsConnected = false;
        }
        initializeBackButton(getView());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.c4_fragment_launcher, container, false);

        mFocusListener = UiUtilities.defaultOnFocusChangeListener((MainActivity) getActivity());

        mProviderGrid = (GridView) root.findViewById(R.id.grid_active);
        mProviderToDownloadGrid = (GridView) root.findViewById(R.id.grid_inactive);

        mSelectAppHint = (TextView) root.findViewById(R.id.text_select_hint);
        mNoAppsWarning = (TextView) root.findViewById(R.id.no_auto_apps_warning);

        initializeScrollView(root);
        initializeNowPlayingProviderDisplay(root);
        initializeBackButton(root);
        initializeAdapters(root);
        mPaginationController.initializePagination(root, mFocusListener);

        setGridAdapter(mProviderGrid, mProviderAdapter);
        setGridAdapter(mProviderToDownloadGrid, mProviderToDownloadAdapter);

        if (mNowPlayingProvider != null) {
            final int color = mNowPlayingProvider.getDisplayInfo().colorAccent;
            final Drawable selector = mProviderGrid.getSelector();
            selector.setTintMode(PorterDuff.Mode.MULTIPLY);
            selector.setTint(color);

            ScrollView scrollView = (ScrollView) root.findViewById(R.id.scrollview);
            UiUtilities.setScrollBarTint(scrollView, getResources(), color);

            mPaginationController.changeActiveColor(color);
        }

        mProviderGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RsEventBus.postSticky(new ProviderConnectedEvent(null, false, false));
                ProviderView providerView = (ProviderView) mProviderAdapter.getItem(i);

                final boolean noCurrentProvider = mNowPlayingProvider == null;
                final boolean hasSomethingToPlay
                        = providerView.getCurrentMetadata() != null
                        && providerView.getCurrentMetadata().isTitleEmpty() == false;
                final boolean isNowPlaying
                        = mNowPlayingProvider != null
                        && mNowPlayingProvider.hasSameNameAs(providerView);
                final boolean showPlayer = (noCurrentProvider && hasSomethingToPlay) || isNowPlaying;
                onProviderSelected(providerView, showPlayer);
            }
        });

        mProviderToDownloadGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ProviderToDownloadView providerToDownloadView = (ProviderToDownloadView) mProviderToDownloadAdapter.getItem(i);
                if (providerToDownloadView.getDisplayInfo().link != null) {
                    Uri uri = providerToDownloadView.getDisplayInfo().link;
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } else {
                    final Resources resources = getResources();
                    final String appName = resources.getString(R.string.app_name);
                    final String rawMessage = resources.getString(R.string.ml_not_connected);
                    final String message = String.format(rawMessage, appName);

                    UiUtilities.showDialog(getActivity(), message);
                }
            }
        });

        return root;
    }

    private void enableDriveMode(boolean enable) {
        enablePagination(enable);
        mInDriveMode = enable;

        if (mProviderAdapter != null) {
            handleGridsVisibility(mProviderAdapter.getCount());
        }
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
            RsEventBus.post(new StartBrowsingEvent(providerView));
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
                = mHeadUnitIsConnected && getFragmentManager().getBackStackEntryCount() == 0;
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
    }

    private void showWarningVisibility(boolean visible) {
        if (mNoAppsWarning == null) return;
        UiUtilities.setVisibility(mNoAppsWarning, visible);
    }

    private void handleGridsVisibility(int activeCount) {
        if (mProviderGrid == null || mProviderToDownloadGrid == null) return;

        if (mInDriveMode) {
            if (activeCount <= 0) {
                showWarningVisibility(true);
                mProviderGrid.setVisibility(View.GONE);
                mProviderToDownloadGrid.setVisibility(View.GONE);
            } else {
                showWarningVisibility(false);
                mProviderGrid.setVisibility(View.VISIBLE);
                mProviderToDownloadGrid.setVisibility(View.GONE);
            }
        } else {
            showWarningVisibility(false);
            if (activeCount <= 0) {
                mProviderGrid.setVisibility(View.GONE);
                mProviderToDownloadGrid.setVisibility(View.VISIBLE);
            } else {
                mProviderGrid.setVisibility(View.VISIBLE);
                mProviderToDownloadGrid.setVisibility(View.GONE);
            }
        }
    }

    private void initializeAdapters(View root) {
        ArrayList<ProviderView> activeProviders = new ArrayList<>();
        if (mListProviders != null) {
            for (ProviderView provider : mListProviders) {
                if (provider.canConnect()) {
                    activeProviders.add(provider);
                }
            }
        }
        ArrayList<ProviderToDownloadView> inactiveProviders = new ArrayList<>();
        if (mListProvidersToDownload != null) {
            for (ProviderToDownloadView provider : mListProvidersToDownload) {
                    inactiveProviders.add(provider);
            }
        }

        mProviderAdapter = new LauncherProviderGridAdapter(this, activeProviders, mUsePagination);
        mProviderToDownloadAdapter = new LauncherProviderToDownloadGridAdapter(this, inactiveProviders, mUsePagination);

        if (mProviderGrid != null) {
            setGridAdapter(mProviderGrid, mProviderAdapter);
        }
        if (mProviderToDownloadGrid != null) {
            setGridAdapter(mProviderToDownloadGrid, mProviderToDownloadAdapter);
        }

        mPaginationController = new PaginationController(mProviderAdapter, mUsePagination);
        mPaginationController.initializePagination(root, mFocusListener);

        mProviderAdapter.notifyDataSetChanged();
        mProviderToDownloadAdapter.notifyDataSetChanged();
        handleGridsVisibility(activeProviders.size());
    }

    private static void setGridAdapter(GridView grid, LauncherProviderGridAdapter adapter) {
        grid.setAdapter(adapter);
        adapter.setOwner(grid);
    }
    private static void setGridAdapter(GridView grid, LauncherProviderToDownloadGridAdapter adapter) {
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
