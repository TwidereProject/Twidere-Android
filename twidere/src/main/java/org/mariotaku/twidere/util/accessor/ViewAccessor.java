/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util.accessor;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

public final class ViewAccessor {

    public static void enableHwAccelIfNecessary(final View view) {
        if (ViewCompat.getLayerType(view) != ViewCompat.LAYER_TYPE_HARDWARE) {
            ViewCompat.setLayerType(view, ViewCompat.LAYER_TYPE_HARDWARE, null);
        }
    }

    public static boolean isInLayout(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return false;
        } else {
            return ViewAccessorJBMR2.isInLayout(view);
        }
    }

    @SuppressWarnings("deprecation")
    public static void setBackground(final View view, final Drawable background) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(background);
        } else {
            ViewAccessorJB.setBackground(view, background);
        }
    }

    public static void setBackgroundTintList(final View view, final ColorStateList list) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setBackgroundTintList(view, list);
    }

    public static void setButtonTintList(CompoundButton view, ColorStateList list) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setButtonTintList(view, list);
    }

    public static void setIndeterminateTintList(ProgressBar view, ColorStateList list) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setIndeterminateTintList(view, list);
    }

    public static void setProgressBackgroundTintList(ProgressBar view, ColorStateList list) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setProgressBackgroundTintList(view, list);
    }

    public static void setProgressTintList(ProgressBar view, ColorStateList list) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setProgressTintList(view, list);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    static class ViewAccessorJB {
        static void setBackground(final View view, final Drawable background) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
            view.setBackground(background);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    static class ViewAccessorJBMR2 {
        static boolean isInLayout(final View view) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) return false;
            return view.isInLayout();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    static class ViewAccessorL {
        static void setBackgroundTintList(final View view, final ColorStateList list) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
            view.setBackgroundTintList(list);
        }

        static void setButtonTintList(final CompoundButton view, final ColorStateList list) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
            view.setButtonTintList(list);
        }

        static void setIndeterminateTintList(final ProgressBar view, final ColorStateList list) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
            view.setIndeterminateTintList(list);
        }

        static void setProgressBackgroundTintList(final ProgressBar view, final ColorStateList list) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
            view.setProgressBackgroundTintList(list);
        }

        static void setProgressTintList(final ProgressBar view, final ColorStateList list) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
            view.setProgressTintList(list);
        }
    }
}
