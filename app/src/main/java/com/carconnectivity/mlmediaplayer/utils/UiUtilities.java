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

package com.carconnectivity.mlmediaplayer.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ScrollView;

import com.carconnectivity.mlmediaplayer.R;
import com.carconnectivity.mlmediaplayer.mediabrowser.ProviderViewActive;
import com.carconnectivity.mlmediaplayer.ui.MainActivity;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by belickim on 07/05/15.
 */
public class UiUtilities {

    private static final String TAG = UiUtilities.class.getSimpleName();

    private static View.OnTouchListener generateOnTouchListener(Context context) {
        final GestureDetector detector
                = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2
                    , float velocityX, float velocityY
            ) {
                return true;
            }
        });
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return detector.onTouchEvent(motionEvent);
            }
        };
    }

    public static void disableInertialScrollingOnList(ListView listView, Context context) {
        listView.setOnTouchListener(generateOnTouchListener(context));
    }

    public static void disableInertialScrolling(GridView gridView, Context context) {
        gridView.setOnTouchListener(generateOnTouchListener(context));
    }

    public static boolean isContrastRequirementMet(int colorA, int colorB) {
        final double lumA = calculateLuminance(colorA);
        final double lumB = calculateLuminance(colorB);

        return contrastRatio(lumA, lumB) >= 7 || contrastRatio(lumB, lumA) >= 7;
    }

    private static double contrastRatio(double L1, double L2) {
        return (L1 + 0.05) / (L2 + 0.05);
    }

    private static double calculateSubPixelLuminance(double subPixelValue) {
        return subPixelValue < 0.03928
                ? subPixelValue / 12.92
                : Math.pow((subPixelValue + 0.055) / 1.055, 2.4);
    }

    private static double calculateLuminance(int color) {
        /* variable names as in definition: http://www.w3.org/TR/WCAG20-TECHS/G17.html */
        final double r = Color.red(color) / 255.0;
        final double g = Color.green(color) / 255.0;
        final double b = Color.blue(color) / 255.0;

        final double R = calculateSubPixelLuminance(r);
        final double G = calculateSubPixelLuminance(g);
        final double B = calculateSubPixelLuminance(b);

        final double L = 0.2126 * R + 0.7152 * G + 0.0722 * B;
        return L;
    }

    public static void performDelayedInUiThread(final Activity activity, final Runnable task, int delay) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                activity.runOnUiThread(task);
            }
        }, delay);
    }

    public static void setScrollBarTint(View view, Resources res, int color) {
        try {
            Field mScrollCacheField = View.class.getDeclaredField("mScrollCache");
            mScrollCacheField.setAccessible(true);
            Object mScrollCache = mScrollCacheField.get(view);

            Field scrollBarField = mScrollCache.getClass().getDeclaredField("scrollBar");
            scrollBarField.setAccessible(true);
            Object scrollBar = scrollBarField.get(mScrollCache);

            Field verticalThumbField = scrollBar.getClass().getDeclaredField("mVerticalThumb");
            verticalThumbField.setAccessible(true);
            Object verticalThumb = verticalThumbField.get(scrollBar);

            final Drawable thumb = (Drawable) verticalThumb;
            thumb.setTintMode(PorterDuff.Mode.MULTIPLY);
            thumb.setTint(color);
        } catch (Exception e) {
            Log.e(TAG, "Something went wrong: ", e);
        }
    }

    public static View.OnFocusChangeListener defaultOnFocusChangeListener(final MainActivity activity) {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ProviderViewActive providerView = activity.getNowPlayingProvider();
                    if (providerView != null && v.getBackground() != null) {
                        v.getBackground().setTintMode(PorterDuff.Mode.MULTIPLY);
                        v.getBackground().setTintList(ColorStateList.valueOf(providerView.getColorAccent()));
                    }
                } else if (v.getBackground() != null) {
                    v.getBackground().setTintMode(PorterDuff.Mode.MULTIPLY);
                    v.getBackground().setTintList(ColorStateList.valueOf(activity.getResources().getColor(R.color.player_background)));
                }
            }
        };
    }

    public static void setVisibility(View view, boolean isVisible) {
        view.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    public static Dialog showDialog(Context context, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.mipmap.ic_warning);
        builder.setTitle(R.string.mirrorlink_dialog_title);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        return dialog;
    }

    private static final int MAX_TEXT_LENGTH = 30;

    /**
     * Check text max size, if exceeded add "..." at the end.
     * We are doing this manually as text view ellipsize calculates layout width and we need
     * cut text because of driver distraction guidelines, not the layout limitations.
     *
     * @param source string to be checked
     * @return modified string if max length is exceeded, source otherwise
     */
    public static String trimLabelText(String source) {
        if (source != null && source.length() > MAX_TEXT_LENGTH) {
            return source.substring(0, MAX_TEXT_LENGTH - 3) + "...";
        }
        return source;
    }

}
