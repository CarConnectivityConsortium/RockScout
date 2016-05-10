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

package com.carconnectivity.mlmediaplayer.ui.player;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.carconnectivity.mlmediaplayer.mediabrowser.events.MediaButtonClickedEvent;
import com.carconnectivity.mlmediaplayer.mediabrowser.model.MediaButtonData;
import com.carconnectivity.mlmediaplayer.utils.RsEventBus;

/**
 * Media button displayed in player UI
 */
public class MediaButton extends ImageView {

    private MediaButtonData mMediaButtonData;
    private ColorStateList mDefaultBackgroundColor;
    private ColorStateList mHighlightColor;
    private ColorStateList mSwitchModeHighlightColor;
    private ColorStateList mSwitchModeIconColor;
    private ColorStateList mDefaultIconColor;
    private ImageView mBackgroundImage;

    private boolean mInvertMode;
    private boolean mShowBackground;
    private boolean mSwitchMode;

    public MediaButton(Context context) {
        super(context);
    }

    public MediaButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressWarnings("unused")
    public MediaButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    // Sets up default touch and onClick listeners
    public void setup(ImageView background, int defaultBackgroundColor) {
        background.setImageTintMode(PorterDuff.Mode.MULTIPLY);
        // Init background from theme
        mDefaultIconColor = ColorStateList.valueOf(Color.WHITE);
        this.mBackgroundImage = background;
        this.mDefaultBackgroundColor = ColorStateList.valueOf(defaultBackgroundColor);
        setMediaButtonData(MediaButtonData.createEmptyMediaButtonData());
        // Set up touch listener for custom button highlight
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mMediaButtonData == null || mMediaButtonData.type == MediaButtonData.Type.EMPTY) {
                    // If button data is empty or null override default behavior so no highlight is shown
                    return true;
                }
                if (mHighlightColor != null && mBackgroundImage != null) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (mSwitchMode) {
                            setImageTintList(mDefaultIconColor);
                        }
                        if (mShowBackground) {
                            mBackgroundImage.setImageTintList(mDefaultBackgroundColor);
                        } else {
                            mBackgroundImage.setVisibility(ImageView.VISIBLE);
                            mBackgroundImage.setAlpha(1.0f);
                        }
                        return true;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (mSwitchMode) {
                            setImageTintList(mSwitchModeIconColor);
                        }
                        if (mShowBackground) {
                            mBackgroundImage.setImageTintList(mSwitchMode ? mSwitchModeHighlightColor : mHighlightColor);
                        } else {
                            mBackgroundImage.setVisibility(ImageView.INVISIBLE);
                            mBackgroundImage.setAlpha(0.0f);
                        }
                        Rect hitRect = new Rect();
                        v.getHitRect(hitRect);
                        if (hitRect.contains((int)event.getX(), (int)event.getY())) {
                            callOnClick();
                        }
                        return true;
                    }
                }
                return false;
            }
        });
        // Set up default on click listener - behavior depends on set MediaButtonData type
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaButtonData != null && mMediaButtonData.type != MediaButtonData.Type.EMPTY) {
                    RsEventBus.post(new MediaButtonClickedEvent(mMediaButtonData));
                }
            }
        });
    }

    public ImageView getBackgroundImage() {
        return mBackgroundImage;
    }

    @Override
    public void setVisibility(int visibility) {
        if (mBackgroundImage != null) {
            if (visibility == ImageView.VISIBLE && mShowBackground) {
                mBackgroundImage.setVisibility(ImageView.VISIBLE);
                mBackgroundImage.setAlpha(1.0f);
            } else {
                mBackgroundImage.setVisibility(ImageView.INVISIBLE);
                mBackgroundImage.setAlpha(0.0f);
            }
        }
        super.setVisibility(visibility);
    }

    public void setInvertMode(boolean enable) {
        if (enable != mInvertMode) {
            mInvertMode = enable;
            updateComponentColorsAndVisibility();
        }
    }

    public void setHighlightColor(int color) {
        mHighlightColor = ColorStateList.valueOf(color);
        updateComponentColorsAndVisibility();
    }

    public void setIconColor(int color) {
        mDefaultIconColor = ColorStateList.valueOf(color);
        updateComponentColorsAndVisibility();
    }

    public void setSwitchModeIconColor(int color) {
        mSwitchModeIconColor = ColorStateList.valueOf(color);
        updateComponentColorsAndVisibility();
    }

    public void setSwitchModeHighlightColor(int color) {
        mSwitchModeHighlightColor = ColorStateList.valueOf(color);
        updateComponentColorsAndVisibility();
    }

    public void setMediaButtonData(MediaButtonData data) {
        this.mMediaButtonData = data;
        if (data.icon != null) {
            setImageDrawable(data.icon);
        } else {
            setImageBitmap(null);
        }
        switch(mMediaButtonData.type) {
            case PAUSE:
            case STOP:
            case PLAY:
                mShowBackground = true;
                mSwitchMode = false;
                break;
            case MORE_ACTIONS_OFF:
                mShowBackground = true;
                mSwitchMode = true;
                break;
            default:
                mShowBackground = false;
                mSwitchMode = false;
                break;
        }
        updateComponentColorsAndVisibility();
        setFocusable(mMediaButtonData.type != MediaButtonData.Type.EMPTY);
    }

    private void updateComponentColorsAndVisibility() {
        if (mBackgroundImage != null && mDefaultBackgroundColor != null) {
            if (!mInvertMode && mHighlightColor != null) {
                mBackgroundImage.setImageTintList(mHighlightColor);
            } else {
                mBackgroundImage.setImageTintList(mDefaultBackgroundColor);
            }
            if (mShowBackground) {
                mBackgroundImage.setVisibility(ImageView.VISIBLE);
                mBackgroundImage.setAlpha(1.0f);
            } else {
                mBackgroundImage.setVisibility(ImageView.INVISIBLE);
                mBackgroundImage.setAlpha(0.0f);
            }
            if (mSwitchMode) {
                setImageTintList(mSwitchModeIconColor);
                mBackgroundImage.setImageTintList(mSwitchModeHighlightColor);
            } else {
                setImageTintList(mDefaultIconColor);
                mBackgroundImage.setImageTintList(mHighlightColor);
            }
        }
    }
}
