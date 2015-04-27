/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util.support.view;

import android.annotation.TargetApi;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

import org.mariotaku.twidere.util.support.graphics.OutlineCompat;
import org.mariotaku.twidere.util.support.DrawableSupport;

/**
 * Interface by which a View builds its {@link org.mariotaku.twidere.util.support.graphics.OutlineCompat}, used for shadow casting and clipping.
 */
public abstract class ViewOutlineProviderCompat {
    /**
     * Called to get the provider to populate the Outline.
     * <p/>
     * This method will be called by a View when its owned Drawables are invalidated, when the
     * View's size changes, or if {@link android.view.View#invalidateOutline()} is called
     * explicitly.
     * <p/>
     * The input outline is empty and has an alpha of <code>1.0f</code>.
     *
     * @param view    The view building the outline.
     * @param outline The empty outline to be populated.
     */
    public abstract void getOutline(View view, OutlineCompat outline);

    /**
     * Maintains the outline of the View to match its rectangular bounds,
     * at <code>1.0f</code> alpha.
     * <p/>
     * This can be used to enable Views that are opaque but lacking a background cast a shadow.
     */
    public static final ViewOutlineProviderCompat BOUNDS = new ViewOutlineProviderCompat() {
        @Override
        public void getOutline(View view, OutlineCompat outline) {
            outline.setRect(0, 0, view.getWidth(), view.getHeight());
        }
    };

    /**
     * Default outline provider for Views, which queries the Outline from the View's background,
     * or generates a 0 alpha, rectangular Outline the size of the View if a background
     * isn't present.
     *
     * @see android.graphics.drawable.Drawable#getOutline(android.graphics.Outline)
     */
    public static final ViewOutlineProviderCompat BACKGROUND = new ViewOutlineProviderCompat() {
        @Override
        public void getOutline(View view, OutlineCompat outline) {
            Drawable background = view.getBackground();
            if (background != null) {
                DrawableSupport.getOutline(background, outline);
            } else {
                outline.setRect(0, 0, view.getWidth(), view.getHeight());
                outline.setAlpha(0.0f);
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static class ViewOutlineProviderL extends ViewOutlineProvider {

        private final ViewOutlineProviderCompat providerCompat;

        public ViewOutlineProviderL(ViewOutlineProviderCompat providerCompat) {
            this.providerCompat = providerCompat;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            providerCompat.getOutline(view, new OutlineCompat.OutlineL(outline));
        }
    }
}
