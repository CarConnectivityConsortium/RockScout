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

import android.app.Fragment;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderToDownloadView;
import com.carconnectivity.mlmediaplayer.utils.pagination.PaginatedAdapter;
import com.carconnectivity.mlmediaplayer.utils.pagination.PaginatedCollection;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by belickim on 16/04/15.
 */
public class LauncherProviderToDownloadGridAdapter extends BaseAdapter implements PaginatedAdapter {
    public final static int PAGE_SIZE = 4;
    public final static int MAX_UNPAGINATED_ITEMS = 256;

    final private boolean mUsePagination;
    final private Fragment mParentFragment;
    private GridView mOwner;
    private PaginatedCollection<ProviderToDownloadView> mItems;
    private final Comparator<ProviderToDownloadView> mProvidersOrder;

    public LauncherProviderToDownloadGridAdapter(Fragment parent, List<ProviderToDownloadView> initialItems, boolean usePagination) {
        mUsePagination = usePagination;
        mParentFragment = parent;
        mProvidersOrder = new Comparator<ProviderToDownloadView>() {
            @Override
            public int compare(ProviderToDownloadView a, ProviderToDownloadView b) {
                final String displayNameA = a.getDisplayInfo().label;
                final String displayNameB = b.getDisplayInfo().label;

                //two providers to download
                if(a.getDisplayInfo().iconDraw == null && b.getDisplayInfo().iconDraw == null) {
                    return displayNameA.compareTo(displayNameB);
                }
                //two providers offline
                if(a.getDisplayInfo().iconDraw != null && b.getDisplayInfo().iconDraw != null) {
                    return displayNameA.compareTo(displayNameB);
                }
                //one provider to download and one offline
                if(b.getDisplayInfo().iconDraw != null && b.getDisplayInfo().iconDraw == null) {
                    return -1;
                }else {
                    return 1;
                }
            }
        };

        final int pageSize = mUsePagination ? PAGE_SIZE : MAX_UNPAGINATED_ITEMS;
        if (mItems == null) {
            mItems = new PaginatedCollection<>(initialItems, pageSize, mProvidersOrder);
        }
    }

    public void setOwner(GridView grid) {
        mOwner = grid;
    }

    public void addItem(ProviderToDownloadView provider) {
        if (mItems == null) {
            ArrayList<ProviderToDownloadView> initialItems = new ArrayList();
            initialItems.add(provider);
            final int pageSize = mUsePagination ? PAGE_SIZE : MAX_UNPAGINATED_ITEMS;
            mItems = new PaginatedCollection<ProviderToDownloadView>(initialItems, pageSize, mProvidersOrder);
        } else if (mItems.contains(provider) == false) {
            mItems.add(provider);
            notifyDataSetChanged();
        }
    }

    public void removeItems(){
        mItems = null;
        notifyDataSetChanged();
    }

    @Override
    public void goToPage(int pageNumber) {
        /* This will prevent focus from staying in the same place after changing page,
         * this causes focus to stay on empty items */
        if (mOwner != null) {
            mOwner.setSelection(0);
        }
        if (mItems!=null && mItems.goToPage(pageNumber)) {
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mItems != null ? mItems.getCurrentItemsCount() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mItems.getCurrentItems().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = mParentFragment.getActivity().getLayoutInflater();
            convertView = inflater.inflate(R.layout.c4_gridview_app, null);
            viewHolder = createViewHolder(convertView);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ProviderToDownloadView provider = (ProviderToDownloadView) getItem(position);
        initializeViews(viewHolder, provider);

        return convertView;
    }

    private void setInactiveStyle(ViewHolder viewHolder) {
        viewHolder.appName.setTextColor(0x88ffffff);
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0.0f);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        viewHolder.appIcon.setColorFilter(filter);
        viewHolder.appIcon.setAlpha(0.4f);
        Drawable color2 = mParentFragment.getResources().getDrawable(R.drawable.c4_launcher_icon_background, null);
        color2.setTint(0x22ffffff);
        viewHolder.appIconInactivityMark.setImageDrawable(color2);
        viewHolder.appIconInactivityMark.setVisibility(View.VISIBLE);
        viewHolder.appIconBackground.setVisibility(View.INVISIBLE);
    }

    private void initializeViews(ViewHolder viewHolder, ProviderToDownloadView provider) {
        final ProviderToDownloadView.ProviderDisplayInfo info = provider.getDisplayInfo();
        viewHolder.appName.setText(info.label);
        viewHolder.appName.setTextColor(0xffffffff);
        viewHolder.appIconAdditional.setVisibility(View.VISIBLE);

        if(info.iconUrl != null) {
            Picasso.with(mParentFragment.getActivity()).load(info.iconUrl.toString()).into(viewHolder.appIcon);
            Picasso.with(mParentFragment.getActivity()).load(R.drawable.ic_shopping_cart).into(viewHolder.appIconAdditional);
        } else {
            viewHolder.appIcon.setImageDrawable(info.iconDraw);
            Picasso.with(mParentFragment.getActivity()).load(R.drawable.ic_usb_plug).into(viewHolder.appIconAdditional);
            setInactiveStyle(viewHolder);
        }

        Drawable color = mParentFragment.getResources().getDrawable(R.drawable.c4_launcher_icon_background, null);
        color.setTint(info.colorPrimaryDark);
        viewHolder.appIconBackground.setImageDrawable(color);
        viewHolder.appIconInactivityMark.setVisibility(View.INVISIBLE);
        viewHolder.appIconBackground.setVisibility(View.VISIBLE);
    }

    private ViewHolder createViewHolder(View convertView) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.appIconInactivityMark
                = (ImageView) convertView.findViewById(R.id.app_icon_inactivity_mark);
        viewHolder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
        viewHolder.appIconBackground
                = (ImageView) convertView.findViewById(R.id.app_icon_background);
        viewHolder.appIconAdditional
                = (ImageView) convertView.findViewById(R.id.app_icon_additional);
        viewHolder.appName = (TextView) convertView.findViewById(R.id.app_name);
        convertView.setTag(viewHolder);
        return viewHolder;
    }

    @Override
    public int getPagesCount() {
        return mItems.getPagesCount();
    }

    @Override
    public int getCurrentPage() {
        return mItems.getCurrentPage();
    }

    private class ViewHolder {
        public ImageView appIcon;
        public ImageView appIconBackground;
        public ImageView appIconInactivityMark;
        public ImageView appIconAdditional;
        public TextView appName;
    }
}
