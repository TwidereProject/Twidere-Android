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
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

public final class ViewAccessor {

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
        public static void setClipToOutline(View view, boolean clipToOutline) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
            view.setClipToOutline(clipToOutline);
        }

        public static void setOutlineProvider(View view, ViewOutlineProviderCompat outlineProvider) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
            view.setOutlineProvider(new ViewOutlineProviderL(outlineProvider));
        }

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


    /**
     * Interface by which a View builds its {@link org.mariotaku.twidere.util.accessor.ViewAccessor.OutlineCompat}, used for shadow casting and clipping.
     */
    public static abstract class ViewOutlineProviderCompat {
        /**
         * Called to get the provider to populate the Outline.
         * <p/>
         * This method will be called by a View when its owned Drawables are invalidated, when the
         * View's size changes, or if {@link View#invalidateOutline()} is called
         * explicitly.
         * <p/>
         * The input outline is empty and has an alpha of <code>1.0f</code>.
         *
         * @param view    The view building the outline.
         * @param outline The empty outline to be populated.
         */
        public abstract void getOutline(View view, OutlineCompat outline);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class ViewOutlineProviderL extends ViewOutlineProvider {

        private final ViewOutlineProviderCompat providerCompat;

        ViewOutlineProviderL(ViewOutlineProviderCompat providerCompat) {
            this.providerCompat = providerCompat;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            providerCompat.getOutline(view, new OutlineL(outline));
        }
    }

    @SuppressWarnings("unused")
    public static abstract class OutlineCompat {

        public abstract float getAlpha();

        public abstract boolean isEmpty();

        public abstract void set(Outline src);

        public abstract void setAlpha(float alpha);

        public abstract void setConvexPath(Path convexPath);

        public abstract void setOval(int left, int top, int right, int bottom);

        public abstract void setOval(Rect rect);

        public abstract void setEmpty();

        public abstract boolean canClip();

        public abstract void setRect(int left, int top, int right, int bottom);

        public abstract void setRect(Rect rect);

        public abstract void setRoundRect(int left, int top, int right, int bottom, float radius);

        public abstract void setRoundRect(Rect rect, float radius);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static class OutlineL extends OutlineCompat {

        private final Outline outline;

        @Override
        public void setEmpty() {
            outline.setEmpty();
        }

        @Override
        public boolean canClip() {
            return outline.canClip();
        }

        @Override
        public void setRect(int left, int top, int right, int bottom) {
            outline.setRect(left, top, right, bottom);
        }

        @Override
        public void setOval(int left, int top, int right, int bottom) {
            outline.setOval(left, top, right, bottom);
        }

        @Override
        public void setConvexPath(Path convexPath) {
            outline.setConvexPath(convexPath);
        }

        @Override
        public void setRect(Rect rect) {
            outline.setRect(rect);
        }

        @Override
        public void setOval(Rect rect) {
            outline.setOval(rect);
        }

        @Override
        public float getAlpha() {
            return outline.getAlpha();
        }

        @Override
        public void set(Outline src) {
            outline.set(src);
        }

        @Override
        public void setRoundRect(int left, int top, int right, int bottom, float radius) {
            outline.setRoundRect(left, top, right, bottom, radius);
        }

        @Override
        public void setRoundRect(Rect rect, float radius) {
            outline.setRoundRect(rect, radius);
        }

        @Override
        public boolean isEmpty() {
            return outline.isEmpty();
        }

        @Override
        public void setAlpha(float alpha) {
            outline.setAlpha(alpha);
        }

        public OutlineL(Outline outline) {
            this.outline = outline;
        }
    }

}
