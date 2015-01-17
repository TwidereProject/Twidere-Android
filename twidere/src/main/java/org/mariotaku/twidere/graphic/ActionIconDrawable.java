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

package org.mariotaku.twidere.graphic;

import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;

import org.mariotaku.twidere.util.menu.TwidereMenuInfo;

/**
 * Created by mariotaku on 15/1/16.
 */
public class ActionIconDrawable extends DrawableWrapper {

    private final int mDefaultColor;

    public ActionIconDrawable(Drawable drawable, int defaultColor) {
        super(drawable);
        mDefaultColor = defaultColor;
        setHighlightColor(0);
    }

    public static void setMenuHighlight(MenuItem item, TwidereMenuInfo info) {
        final Drawable icon = item.getIcon();
        if (icon instanceof ActionIconDrawable) {
            ((ActionIconDrawable) icon).setHighlightColor(info.isHighlight() ? info.getHighlightColor(0) : 0);
        }
    }

    private void setHighlightColor(int color) {
        setColorFilter(color == 0 ? mDefaultColor : color, Mode.SRC_ATOP);
    }

}
