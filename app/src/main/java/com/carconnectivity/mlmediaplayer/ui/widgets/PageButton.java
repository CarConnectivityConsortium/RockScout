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

package com.carconnectivity.mlmediaplayer.ui.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.carconnectivity.mlmediaplayer.R;

/**
 * Created by belickim on 15/05/15.
 */
public class PageButton extends FrameLayout {
    private boolean mIsActive = false;
    private boolean mIsNextButton = false;
    private boolean mIsPreviousButton = false;
    private int mPageNumber = 0;
    private int mCurrentActiveColor;

    private final ImageView mBackground;
    private final ImageView mNextIcon;
    private final ImageView mPreviousIcon;
    private final Button mButton;

    public PageButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        final LayoutInflater inflater
                = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.c4_page_button, this);

        mBackground = (ImageView) findViewById(R.id.page_button_background);
        mNextIcon = (ImageView) findViewById(R.id.page_button_next_icon);
        mPreviousIcon = (ImageView) findViewById(R.id.page_button_previous_icon);
        mButton = (Button) findViewById(R.id.page_button_button);

        mCurrentActiveColor = getResources().getColor(R.color.c4_active_button_color);

        update();
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mButton.setOnClickListener(listener);
    }

    public boolean isNextButton() {
        return mIsNextButton;
    }

    public void setActiveColor(int color) {
        mCurrentActiveColor = color;
        update();
    }

    public void setButtonClickable(boolean clickable) {
        mButton.setClickable(clickable);
    }

    public void applyFocusListener(View.OnFocusChangeListener listener) {
        mButton.setOnFocusChangeListener(listener);
    }

    public void setActive(boolean isActive) {
        mIsActive = isActive;
        update();
    }

    public void setShowNextIcon(boolean showNext) {
        mIsNextButton = showNext;
        mIsPreviousButton = false;
        update();
    }

    public void setShowPreviousIcon(boolean showPrevious) {
        mIsPreviousButton = showPrevious;
        mIsNextButton = false;
        update();
    }

    public void setPageNumber(int pageNumber) {
        mPageNumber = pageNumber;
        update();
    }

    private void update() {
        final int backgroundColor
                = mIsActive
                ? mCurrentActiveColor
                : getResources().getColor(R.color.c4_inactive_button_color)
                ;

        mBackground.setImageTintList(ColorStateList.valueOf(backgroundColor));
        mNextIcon.setVisibility(mIsNextButton ? VISIBLE : INVISIBLE);
        mPreviousIcon.setVisibility(mIsPreviousButton ? VISIBLE : INVISIBLE);
        mButton.setText(mIsNextButton || mIsPreviousButton ? "" : Integer.toString(mPageNumber));
        mButton.setFocusable(mIsNextButton || mIsPreviousButton);
    }
}
