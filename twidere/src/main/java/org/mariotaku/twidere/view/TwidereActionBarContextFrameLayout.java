/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.internal.widget.ActionBarContainer;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.iface.IThemedActivity;
import org.mariotaku.twidere.util.ThemeUtils;

/**
 * Created by mariotaku on 15/4/28.
 */
public class TwidereActionBarContextFrameLayout extends FrameLayout {

    private static final int[] ATTRS = {android.R.attr.layout};

    public TwidereActionBarContextFrameLayout(Context context, AttributeSet attrs) {
        super(wrapContext(context), attrs);
        final TypedArray a = getContext().obtainStyledAttributes(attrs, ATTRS);
        inflate(getContext(), a.getResourceId(0, 0), this);
        a.recycle();
    }

    private static Context wrapContext(Context context) {
        if (context instanceof IThemedActivity) {
            return ThemeUtils.getActionBarThemedContext(context,
                    ((IThemedActivity) context).getCurrentThemeResourceId(),
                    ((IThemedActivity) context).getCurrentThemeColor());
        }
        return ThemeUtils.getActionBarThemedContext(context);
    }
}
