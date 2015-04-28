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

package org.mariotaku.twidere.view;

import android.content.Context;
import android.support.v7.internal.widget.ActionBarContainer;
import android.util.AttributeSet;

import org.mariotaku.twidere.util.ThemeUtils;

/**
 * Created by mariotaku on 15/4/28.
 */
public class TwidereActionBarContainer extends ActionBarContainer {
    public TwidereActionBarContainer(Context context) {
        super(wrapContext(context));
    }

    public TwidereActionBarContainer(Context context, AttributeSet attrs) {
        super(wrapContext(context), attrs);
    }

    private static Context wrapContext(Context context) {
        return ThemeUtils.getActionBarThemedContext(context);
    }
}
