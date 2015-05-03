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
import android.support.v7.internal.widget.ActionBarContextView;
import android.support.v7.view.ActionMode;
import android.util.AttributeSet;

import org.mariotaku.twidere.util.ThemeUtils;

/**
 * Created by mariotaku on 15/1/16.
 */
public class TwidereActionBarContextView extends ActionBarContextView {

    private int mItemColor;

    public TwidereActionBarContextView(Context context) {
        super(context);
    }

    public TwidereActionBarContextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TwidereActionBarContextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void initForMode(ActionMode mode) {
        super.initForMode(mode);
        if (mItemColor != 0) {
            ThemeUtils.setActionBarContextViewColor(this, mItemColor);
        }
    }

    public void setItemColor(int itemColor) {
        mItemColor = itemColor;
        ThemeUtils.setActionBarContextViewColor(this, itemColor);
    }
}
