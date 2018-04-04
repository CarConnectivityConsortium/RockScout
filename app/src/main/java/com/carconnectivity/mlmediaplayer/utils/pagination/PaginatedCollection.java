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

package com.carconnectivity.mlmediaplayer.utils.pagination;

import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by belickim on 15/05/15.
 */
public class PaginatedCollection<T> {
    private final Comparator<T> mOrder;
    private PaginationModel mModel;
    private ArrayList<T> mItems;

    /**
     * Creates new instance obviously.
     *
     * @param items    initial set of items.
     * @param pageSize maximum count of items on a single page.
     * @param order    comparator to order the items, if null the order is preserved as given.
     */
    public PaginatedCollection(Collection<T> items, int pageSize, Comparator<T> order) {
        if (pageSize < 1)
            throw new IllegalArgumentException("Page size must be positive integer.");

        mItems = new ArrayList<>();
        for (T item : items)
            if (contains(item) == false)
                mItems.add(item);

        mModel = new PaginationModel(mItems.size(), pageSize);
        mOrder = order;

        sortItems();
    }

    private void sortItems() {
        if (mItems == null || mOrder == null) return;
        Collections.sort(mItems, mOrder);
    }

    public int getCurrentItemsCount() {
        return mModel.getCountOfItemsOnCurrentPage();
    }

    public List<T> getCurrentItems() {
        int[] indices = mModel.getVisibleItemIndices();
        final ArrayList<T> items = new ArrayList<>(mModel.getPageSize());
        for (int index : indices) {
            items.add(mItems.get(index));
        }
        return items;
    }

    public boolean goToPage(int i) {
        return mModel.setCurrentPage(i);
    }

    public void add(T item) {
        mItems.add(item);
        mModel.grow(1);
        sortItems();
    }

    public void addAll(Collection<T> items) {
        mItems.addAll(items);
        mModel.grow(items.size());
        sortItems();
    }

    public int getPagesCount() {
        return mModel.getPagesCount();
    }

    public int getCurrentPage() {
        return mModel.getCurrentPage();
    }

    public boolean contains(T item) {
        for (T owned : mItems) {
            /* Your WTF is sponsored by Java's Object.equals, have a nice day. */
            if (owned instanceof ProviderView && item instanceof ProviderView) {
                ProviderView ownedView = (ProviderView) owned;
                ProviderView itemView = (ProviderView) item;
                if (itemView.hasSameIdAs(ownedView))
                    return true;
            } else if (owned.equals(item)) {
                return true;
            }
        }
        return false;
    }
}
