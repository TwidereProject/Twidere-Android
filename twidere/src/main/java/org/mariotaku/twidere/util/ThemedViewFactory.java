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

package org.mariotaku.twidere.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater.Factory2;
import android.view.View;

/**
 * Created by mariotaku on 15/1/13.
 */
public class ThemedViewFactory implements Factory2 {

    private final int mThemeColor;

    public ThemedViewFactory(int themeColor) {
        mThemeColor = themeColor;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return ThemeUtils.createView(name, context, attrs, mThemeColor);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return null;
    }
}
