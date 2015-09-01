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

package org.mariotaku.twidere.util.support;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.twidere.util.CompareUtils;
import org.mariotaku.twidere.util.support.view.ViewOutlineProviderCompat;

public final class ViewSupport {

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

    public static void setButtonTintList(CompoundButton view, ColorStateList list) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setButtonTintList(view, list);
    }

    public static ColorStateList getButtonTintList(CompoundButton view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null;
        return ViewAccessorL.getButtonTintList(view);
    }

    public static void setClipToOutline(View view, boolean clipToOutline) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setClipToOutline(view, clipToOutline);
    }

    public static void setIndeterminateTintList(ProgressBar view, ColorStateList list) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setIndeterminateTintList(view, list);
    }

    public static void setOutlineProvider(View view, ViewOutlineProviderCompat outlineProvider) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setOutlineProvider(view, outlineProvider);

    }

    public static void setProgressBackgroundTintList(ProgressBar view, ColorStateList list) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setProgressBackgroundTintList(view, list);
    }

    public static void setProgressTintList(ProgressBar view, ColorStateList list) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setProgressTintList(view, list);
    }

    @SuppressWarnings("unchecked")
    public static <T extends View> T findViewByType(View view, Class<T> cls) {
        if (cls.isAssignableFrom(view.getClass())) return (T) view;
        if (view instanceof ViewGroup) {
            for (int i = 0, j = ((ViewGroup) view).getChildCount(); i < j; i++) {
                final View found = findViewByType(((ViewGroup) view).getChildAt(i), cls);
                if (found != null) return (T) found;
            }
        }
        return null;
    }

    public static TextView findViewByText(View view, CharSequence text) {
        if (view instanceof TextView && CompareUtils.textEquals(text, ((TextView) view).getText()))
            return (TextView) view;
        if (view instanceof ViewGroup) {
            for (int i = 0, j = ((ViewGroup) view).getChildCount(); i < j; i++) {
                final TextView found = findViewByText(((ViewGroup) view).getChildAt(i), text);
                if (found != null) return found;
            }
        }
        return null;
    }

    public static void setForeground(View view, Drawable foreground) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return;
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            ViewAccessorICS.setForeground(view, foreground);
        } else {
            view.setForeground(foreground);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    static class ViewAccessorJB {
        static void setBackground(final View view, final Drawable background) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
            view.setBackground(background);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    static class ViewAccessorICS {
        static void setForeground(final View view, final Drawable foreground) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) return;
            if (view instanceof FrameLayout) {
                //noinspection RedundantCast
                ((FrameLayout) view).setForeground(foreground);
            }
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
        public static void setClipToOutline(View view, boolean clipToOutline) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
            view.setClipToOutline(clipToOutline);
        }

        public static void setOutlineProvider(View view, ViewOutlineProviderCompat outlineProvider) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
            view.setOutlineProvider(new ViewOutlineProviderCompat.ViewOutlineProviderL(outlineProvider));
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

        public static ColorStateList getButtonTintList(CompoundButton view) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null;
            return view.getButtonTintList();
        }
    }


}
