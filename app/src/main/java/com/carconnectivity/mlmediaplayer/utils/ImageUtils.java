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
 * Car Connectivity Consortium products ("Feedback").
 *
 * You agrees that any such Feedback is given on non-confidential
 * basis and Licensee hereby waives any confidentiality restrictions
 * for such Feedback. In addition, Licensee grants to the Car Connectivity Consortium
 * and its affiliates a worldwide, non-exclusive, perpetual, irrevocable,
 * sub-licensable, royalty-free right and license under Licensee's copyrights to copy,
 * reproduce, modify, create derivative works and directly or indirectly
 * distribute, make available and communicate to public the Feedback
 * in or in connection to any CCC products, software and/or services.
 */

package com.carconnectivity.mlmediaplayer.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by belickim on 08/05/15.
 */
public class ImageUtils {

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        final Bitmap bitmap
                = Bitmap.createBitmap( drawable.getIntrinsicWidth()
                                     , drawable.getIntrinsicHeight()
                                     , Bitmap.Config.ARGB_8888
                                     );
        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private static boolean isTransparent(int color, int alphaThreshold) {
        return Color.alpha(color) <= alphaThreshold;
    }

    private static Bitmap trimTransparentRegions(Bitmap input, int alphaThreshold) {
        final int height = input.getHeight();
        final int width = input.getWidth();

        final int[] pixels = new int[height * width];
        input.getPixels(pixels, 0, width, 0, 0, width, height);

        int boundTop = 0, boundBottom = 0, boundLeft = 0, boundRight = 0;
        int i = 0;

        topScan: for (i = 0; i < pixels.length; i++) {
            if (isTransparent(pixels[i], alphaThreshold) == false) {
                boundTop = i / width;
                break topScan;
            }
        }

        bottomScan: for (i = pixels.length - 1; i >= 0; i--) {
            if (isTransparent(pixels[i], alphaThreshold) == false) {
                boundBottom = (pixels.length - i) / width;
                break bottomScan;
            }
        }

        /* for each x, x <- 0, scan downwards until not transparent pixel is reached */
        leftScan: for (i = 0; i < width; i++) {
            for (int j = i; j < pixels.length; j += width) {
                if (isTransparent(pixels[j], alphaThreshold) == false) {
                    boundLeft = j % width;
                    break leftScan;
                }
            }
        }

        /* for each x, x <- width, scan upwards until not transparent pixel is reached */
        rightScan: for (i = pixels.length - 1; i >= 0; i--) {
            for (int j = i; j >= 0; j -= width) {
                if (isTransparent(pixels[j], alphaThreshold) == false) {
                    boundRight = width - (j % width);
                    break rightScan;
                }
            }
        }

        return Bitmap.createBitmap( input, boundLeft, boundTop
                                  , width - boundLeft - boundRight
                                  , height - boundTop - boundBottom
                                  );
    }

    /**
     * Creates new drawable with invisible pixels trimmed at the borders
     * @param input drawable to be trimmed
     * @return trimmed copy of the drawable
     */
    public static Drawable trimTransparent(Drawable input, Resources res) {
        final Bitmap bitmap = drawableToBitmap(input);
        final Bitmap trimmed = trimTransparentRegions(bitmap, 0);
        return new BitmapDrawable(res, trimmed);
    }

}
