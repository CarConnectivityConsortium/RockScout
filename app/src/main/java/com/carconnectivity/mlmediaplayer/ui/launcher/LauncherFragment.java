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
import android.app.Dialog;
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
import android.widget.*;
import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.commonapi.events.DriveModeStatusChangedEvent;
import com.carconnectivity.mlmediaplayer.commonapi.events.MirrorLinkSessionChangedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderView;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderViewActive;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderViewInactive;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderViewToDownload;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.*;
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
    private WeakReference<InteractionListener> mListener;

    private List<ProviderView> mListProviders;

    private GridView mProviderGrid;
    private ProviderViewActive mNowPlayingProvider;

    private TextView mSelectAppHint;
    private TextView mNoAppsWarning;
    private boolean mHeadUnitIsConnected;
    private Dialog mDialog;

    private View.OnFocusChangeListener mFocusListener;

    public static LauncherFragment newInstance() {
        LauncherFragment fragment = new LauncherFragment();
        fragment.mListProviders = new ArrayList<>();
        fragment.mHeadUnitIsConnected = false;

        RsEventBus.registerSticky(fragment);

        return fragment;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        RsEventBus.unregister(this);
    }

    public void clearList() {
        mListProviders.clear();
        if (mProviderAdapter != null) {
            mProviderAdapter.removeItems();
            handleGridsVisibility(0);
        }
    }

    public LauncherFragment() {
        // Required empty public constructor
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(DriveModeStatusChangedEvent event) {
        enablePagination(event.isDriveModeActive);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ProviderDiscoveredEvent event) {
        final ProviderViewActive provider = event.provider;
        addItem(provider);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ProviderToDownloadDiscoveredEvent event) {
        final ProviderViewToDownload provider = event.provider;
        addItem(provider);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ProviderInactiveDiscoveredEvent event) {
        final ProviderViewInactive provider = event.provider;
        addItem(provider);
    }

    private void addItem(ProviderView provider) {
        mListProviders.add(provider);

        if (mProviderAdapter != null) {
            mProviderAdapter.addItem(provider);
            handleGridsVisibility(mProviderAdapter.getCount());
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NowPlayingProviderChangedEvent event) {
        mNowPlayingProvider = event.provider;
        initializeNowPlayingProviderDisplay(getView());
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(MirrorLinkSessionChangedEvent event) {
        mHeadUnitIsConnected = event.headUnitIsConnected;
        if (!mHeadUnitIsConnected) {
            enablePagination(false);
        }
        hideNotSupportedDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.c4_fragment_launcher, container, false);

        mFocusListener = UiUtilities.defaultOnFocusChangeListener((MainActivity) getActivity());

        mProviderGrid = (GridView) root.findViewById(R.id.gridView);

        mSelectAppHint = (TextView) root.findViewById(R.id.text_select_hint);
        mNoAppsWarning = (TextView) root.findViewById(R.id.no_auto_apps_warning);

        ImageButton backButton = (ImageButton) root.findViewById(R.id.launcher_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getFragmentManager().getBackStackEntryCount() == 0) {
                    RsEventBus.post(new FinishActivityEvent());
                }
                getFragmentManager().popBackStack();
            }
        });
        backButton.setOnFocusChangeListener(mFocusListener);

        initializeScrollView(root);
        initializeNowPlayingProviderDisplay(root);
        initializeAdapters(root);
        mPaginationController.initializePagination(root, mFocusListener);

        setGridAdapter(mProviderGrid, mProviderAdapter);

        if (mNowPlayingProvider != null) {
            final int color = mNowPlayingProvider.getColorAccent();
            final Drawable selector = mProviderGrid.getSelector();
            selector.setTintMode(PorterDuff.Mode.MULTIPLY);
            selector.setTint(color);

            GridView gridView = (GridView) root.findViewById(R.id.gridView);
            UiUtilities.setScrollBarTint(gridView, getResources(), color);

            mPaginationController.changeActiveColor(color);
        }

        mProviderGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ProviderView providerView = (ProviderView) mProviderAdapter.getItem(i);
                if (providerView instanceof ProviderViewActive) {
                    ProviderViewActive viewOnline = (ProviderViewActive) providerView;

                    final boolean noCurrentProvider = mNowPlayingProvider == null;
                    final boolean hasSomethingToPlay
                            = viewOnline.getCurrentMetadata() != null
                            && viewOnline.getCurrentMetadata().isTitleEmpty() == false;
                    final boolean isNowPlaying
                            = mNowPlayingProvider != null
                            && mNowPlayingProvider.hasSameIdAs(providerView);
                    final boolean showPlayer = (noCurrentProvider && hasSomethingToPlay) || isNowPlaying;
                    onProviderSelected(viewOnline, showPlayer);
                } else if (providerView instanceof ProviderViewInactive) {
                    final String message = getResources().getString(R.string.ml_not_connected);

                    mDialog = UiUtilities.showDialog(getActivity(), message);
                } else if (providerView instanceof ProviderViewToDownload) {
                    ProviderViewToDownload viewToDownload = (ProviderViewToDownload) providerView;

                    Uri uri = Uri.parse("market://details?id=" + viewToDownload.getId());
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                }
            }
        });
        return root;
    }

    private void onProviderSelected(ProviderViewActive providerView, boolean showPlayer) {
        Log.d(TAG, "onProviderSelected: providerView=" + (providerView != null ? providerView.toString() : "null") + ", showPlayer=" + showPlayer);
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

    private void hideNotSupportedDialog() {
        Log.d(TAG, "hideNotSupportedDialog");
        if (!mHeadUnitIsConnected) return;
        if (mDialog == null) return;

        mDialog.hide();
        mDialog = null;
    }

    private void initializeScrollView(View root) {
        Log.d(TAG, "initializeScrollView");
        if (root == null) return;

        final GridView gridView = (GridView) root.findViewById(R.id.gridView);
        UiUtilities.disableInertialScrolling(gridView, getActivity());
    }

    private void initializeNowPlayingProviderDisplay(View root) {
        Log.d(TAG, "initializeNowPlayingProviderDisplay");
        if (root == null) return;

        String providerName = "";
        Drawable providerIcon = null;

        Button providerButton = (Button) root.findViewById(R.id.buttonLauncher);
        if (mNowPlayingProvider != null) {
            providerName = getResources().getString(R.string.now_playing);
            providerButton.setVisibility(View.VISIBLE);
            providerIcon = mNowPlayingProvider.getNotificationDrawable() != null ?
                    mNowPlayingProvider.getNotificationDrawable() : mNowPlayingProvider.getIconDrawable();
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

    public void refreshPaginationController() {
        Log.d(TAG, "refreshPaginationController");
        mPaginationController.setNumbers();
        if(mProviderAdapter != null){
            mProviderAdapter.notifyDataSetChanged();
        }
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
        Log.d(TAG, "handleGridsVisibility");
        if (mProviderGrid == null) return;

        if (activeCount <= 0) {
            showWarningVisibility(true);
            mProviderGrid.setVisibility(View.INVISIBLE);
        } else {
            showWarningVisibility(false);
            mProviderGrid.setVisibility(View.VISIBLE);
        }
    }

    private void initializeAdapters(View root) {
        ArrayList<ProviderView> activeProviders = new ArrayList<>();
        if (mListProviders != null) {
            for (ProviderView provider : mListProviders) {
                activeProviders.add(provider);
            }
        }

        mProviderAdapter = new LauncherProviderGridAdapter(this, activeProviders, mUsePagination);

        if (mProviderGrid != null) {
            setGridAdapter(mProviderGrid, mProviderAdapter);
        }

        mPaginationController = new PaginationController(mProviderAdapter, mUsePagination);
        mPaginationController.initializePagination(root, mFocusListener);

        mProviderAdapter.notifyDataSetChanged();
        handleGridsVisibility(activeProviders.size());
    }

    private static void setGridAdapter(GridView grid, LauncherProviderGridAdapter adapter) {
        grid.setAdapter(adapter);
        adapter.setOwner(grid);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = new WeakReference<>((InteractionListener) activity);
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
