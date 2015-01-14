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

package org.mariotaku.twidere.view.themed;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.TextView;

import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.iface.IThemedView;

public class ThemedTextView extends TextView implements IThemedView {

    public ThemedTextView(final Context context) {
        this(context, null);
    }

    public ThemedTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public ThemedTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        ThemeUtils.initTextView(this);
    }

    @Override
    public void setThemeTintColor(ColorStateList color) {
        final int linkTextColor = ThemeUtils.getOptimalLinkColor(color.getDefaultColor(), getCurrentTextColor());
        final int red = Color.red(linkTextColor), green = Color.green(linkTextColor), blue = Color.blue(linkTextColor);
        setLinkTextColor(linkTextColor);
        setHighlightColor(Color.argb(0x66, red, green, blue));
    }
}
