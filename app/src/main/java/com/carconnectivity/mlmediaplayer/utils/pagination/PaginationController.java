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

import android.view.View;
import android.widget.LinearLayout;

import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.ui.widgets.PageButton;

/**
 * Created by belickim on 18/05/15.
 */
public class PaginationController {

    private boolean mUsePagination;
    private PaginatedAdapter mAdapter;

    private PageButton mButtonPrevious;
    private PageButton mButtonCurrent;
    private PageButton mButtonNext;

    public PaginationController(PaginatedAdapter adapter, boolean usePagination) {
        mAdapter = adapter;
        mUsePagination = usePagination;
    }

    private void setButtonsActive() {
        mButtonPrevious.setActive(false);
        mButtonCurrent.setActive(true);
        mButtonNext.setActive(false);
    }

    private View.OnClickListener generateListener(final PageButton button) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int direction = button.isNextButton() ? 1 : -1;
                final int pageNumber = mAdapter.getCurrentPage() + direction;
                mAdapter.goToPage(pageNumber);
                setNumbers();
            }
        };
    }

    private void setClickListener(final PageButton button) {
        button.setOnClickListener(generateListener(button));
    }

    public void setVisibleButtons(boolean visible) {
        if(!mUsePagination) return;
        mButtonPrevious.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mButtonCurrent.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mButtonNext.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    public void changeActiveColor(int color) {
        mButtonPrevious.setActiveColor(color);
        mButtonCurrent.setActiveColor(color);
        mButtonNext.setActiveColor(color);
    }

    public void initializePagination
            (View root, View.OnFocusChangeListener focusListener) {
        if (root == null) return;

        LinearLayout paginationControls
                = (LinearLayout) root.findViewById(R.id.launcher_pagination);
        paginationControls.setVisibility(mUsePagination ? View.VISIBLE : View.INVISIBLE);
        mButtonPrevious = (PageButton) root.findViewById(R.id.page_button_previous);
        mButtonCurrent = (PageButton) root.findViewById(R.id.page_button_current);
        mButtonNext = (PageButton) root.findViewById(R.id.page_button_next);
        if (mUsePagination) {
            setNumbers();
            setButtonsActive();

            setClickListener(mButtonPrevious);
            setClickListener(mButtonNext);
        }

        if (focusListener != null) {
            mButtonPrevious.applyFocusListener(focusListener);
            mButtonNext.applyFocusListener(focusListener);
        }

    }

    public void setNumbers() {
        final int currentPage = mAdapter.getCurrentPage();
        final int pagesCount = mAdapter.getPagesCount();

        mButtonPrevious.setShowPreviousIcon(true);
        mButtonCurrent.setPageNumber(currentPage + 1);
        mButtonNext.setShowNextIcon(true);

        mButtonPrevious.setVisibility(currentPage > 0 ? View.VISIBLE : View.INVISIBLE);
        mButtonNext.setVisibility(currentPage < pagesCount - 1 ? View.VISIBLE : View.INVISIBLE);

        mButtonCurrent.setVisibility(pagesCount > 1 ? View.VISIBLE : View.INVISIBLE);
        mButtonCurrent.setButtonClickable(false);
    }
}
