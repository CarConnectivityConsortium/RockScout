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

/**
 * Created by belickim on 15/05/15.
 */
public class PaginationModel {
    private final int mItemsPerPage;

    private int mItemsCount;
    private int mCurrentPage;

    public PaginationModel(int itemsCount, int itemsPerPage) {
        if (itemsCount < 0)
            throw new IllegalArgumentException("Items count cannot be negative.");
        if (itemsPerPage < 1)
            throw new IllegalArgumentException("Items per page count must be positive.");

        mItemsCount = itemsCount;
        mItemsPerPage = itemsPerPage;

        mCurrentPage = 0;
    }

    public int getPagesCount() {
        return (int) Math.ceil(mItemsCount / (double) mItemsPerPage);
    }

    public int getPageSize() {
        return mItemsPerPage;
    }

    public boolean setCurrentPage(int currentPage) {
        if (currentPage < 0) return false;
        if (currentPage >= getPagesCount()) return false;

        mCurrentPage = currentPage;
        return true;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public void grow(int amount) {
        mItemsCount += amount;
    }

    public int getCountOfItemsOnCurrentPage() {
        int itemsBehind = mCurrentPage * mItemsPerPage;
        int itemsHereAndForward = mItemsCount - itemsBehind;
        return Math.min(itemsHereAndForward, mItemsPerPage);
    }

    public int[] getVisibleItemIndices() {
        int visibleItemsCount = getCountOfItemsOnCurrentPage();
        int[] visibleIndices = new int[visibleItemsCount];

        int firstIndex = mCurrentPage * mItemsPerPage;
        for (int i = 0; i < visibleItemsCount; ++i) {
            visibleIndices[i] = firstIndex + i;
        }

        return visibleIndices;
    }
}
