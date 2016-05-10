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
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;

import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.mediabrowser.MediaItemView;
import com.carconnectivity.mlmediaplayer.utils.pagination.PaginatedAdapter;
import com.carconnectivity.mlmediaplayer.utils.pagination.PaginatedCollection;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by belickim on 22/04/15.
 */
public class NavigatorListAdapter extends BaseAdapter implements PaginatedAdapter {
    public final static int PAGE_SIZE = 4;
    public final static int MAX_UNPAGINATED_ITEMS = 1024;

    final private Fragment mParentFragment;
    private ListView mOwner;
    private PaginatedCollection<MediaItemView> mItems;
    private boolean mUsePagination;

    public NavigatorListAdapter(Fragment parent, boolean usePagination) {
        mParentFragment = parent;
        mUsePagination = usePagination;
        ArrayList<MediaItemView> initialItems = new ArrayList<>();
        mItems = new PaginatedCollection<>(initialItems, mUsePagination ? PAGE_SIZE : MAX_UNPAGINATED_ITEMS, null);
    }

    public void setOwner(ListView list) {
        mOwner = list;
    }

    public void setItems(final Collection<MediaItemView> items) {
        mItems = new PaginatedCollection<>(items, mUsePagination ? PAGE_SIZE : MAX_UNPAGINATED_ITEMS, null);
        notifyDataSetChanged();
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
            convertView = inflater.inflate(R.layout.listview_app, null);
            viewHolder = new ViewHolder();
            viewHolder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            viewHolder.appName = (TextView) convertView.findViewById(R.id.app_name);
            viewHolder.appName.setTextColor(0xffffffff);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final MediaItemView view = (MediaItemView) getItem(position);
        viewHolder.appName.setText(view.getDisplayLabel());
        viewHolder.appIcon.setImageDrawable(null);

        Uri displayIconUri = view.getDisplayIconUri();
        Bitmap displayIconBitmap = view.getDisplayIconBitmap();

        //set icon from bitmap or uri
        if(displayIconBitmap != null) {
            viewHolder.appIcon.setImageBitmap(displayIconBitmap);
        } else if(displayIconUri != null) {
            final Context context = mParentFragment.getActivity().getApplicationContext();
            final String rawUri = displayIconUri.toString();
            Picasso.with(context).load(rawUri).into(viewHolder.appIcon);
        }

        return convertView;
    }

    public void clear() {
        mItems = null;
        notifyDataSetChanged();
    }

    public boolean usePagination() {
        return mUsePagination;
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
    public int getPagesCount() {
        return mItems == null ? 0 : mItems.getPagesCount();
    }

    @Override
    public int getCurrentPage() {
        return mItems == null ? 0 : mItems.getCurrentPage();
    }

    private class ViewHolder {
        public ImageView appIcon;
        public TextView appName;
    }
}
