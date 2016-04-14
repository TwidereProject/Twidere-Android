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

package org.mariotaku.twidere.util.support;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.TextView;

/**
 * Created by mariotaku on 15/5/3.
 */
public class TextViewSupport {
    private TextViewSupport() {
    }

    public static Drawable[] getCompoundDrawablesRelative(TextView view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return view.getCompoundDrawables();
        }
        return TextViewSupportJBMR1.getCompoundDrawablesRelative(view);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static class TextViewSupportJBMR1 {
        public static Drawable[] getCompoundDrawablesRelative(TextView view) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return view.getCompoundDrawables();
            }
            return view.getCompoundDrawablesRelative();
        }
    }
}
