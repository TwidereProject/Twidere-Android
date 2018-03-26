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
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;

import org.mariotaku.twidere.util.support.view.ViewOutlineProviderCompat;
import org.mariotaku.twidere.view.iface.IForegroundView;

public final class ViewSupport {

    private ViewSupport() {
    }

    public static void setClipToOutline(View view, boolean clipToOutline) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setClipToOutline(view, clipToOutline);
    }

    public static void setOutlineProvider(View view, ViewOutlineProviderCompat outlineProvider) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        ViewAccessorL.setOutlineProvider(view, outlineProvider);

    }

    public static void setForeground(View view, Drawable foreground) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            ViewAccessorICS.setForeground(view, foreground);
        } else {
            view.setForeground(foreground);
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    static class ViewAccessorICS {
        private ViewAccessorICS() {
        }

        static void setForeground(final View view, final Drawable foreground) {
            if (view instanceof FrameLayout) {
                //noinspection RedundantCast
                ((FrameLayout) view).setForeground(foreground);
            } else if (view instanceof IForegroundView) {
                //noinspection RedundantCast
                ((IForegroundView) view).setForeground(foreground);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    static class ViewAccessorL {
        private ViewAccessorL() {
        }

        public static void setClipToOutline(View view, boolean clipToOutline) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
            view.setClipToOutline(clipToOutline);
        }

        public static void setOutlineProvider(View view, @Nullable ViewOutlineProviderCompat outlineProvider) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
            view.setOutlineProvider(new ViewOutlineProviderCompat.ViewOutlineProviderL(outlineProvider));
        }


    }


}
