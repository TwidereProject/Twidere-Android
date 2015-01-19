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

package org.mariotaku.twidere.view.themed;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.iface.IThemedView;

public class ThemedSwitchCompat extends SwitchCompat implements IThemedView {

    public ThemedSwitchCompat(final Context context) {
        this(context, null);
    }

    public ThemedSwitchCompat(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        ThemeUtils.initTextView(this);
    }

    @Override
    public void setThemeTintColor(ColorStateList color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            DrawableCompat.setTintList(getThumbDrawable(), color);
            DrawableCompat.setTintList(getTrackDrawable(), color);
        }
    }
}
