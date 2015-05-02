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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.ActionMenuPresenter;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;

import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.Utils;

/**
 * Created by mariotaku on 15/1/16.
 */
public class TwidereToolbar extends Toolbar {

    private int mItemColor;

    public TwidereToolbar(Context context) {
        super(context);
    }

    public TwidereToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TwidereToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public Menu getMenu() {
        final Menu menu = super.getMenu();
        ThemeUtils.setActionBarOverflowColor(this, mItemColor);
        final ActionMenuView menuView = (ActionMenuView) Utils.findFieldOfTypes(this, Toolbar.class,
                ActionMenuView.class);
        if (menuView != null) {
            final ActionMenuPresenter presenter = (ActionMenuPresenter) Utils.findFieldOfTypes(menuView,
                    ActionMenuView.class, ActionMenuPresenter.class);
            if (presenter != null) {
                ThemeUtils.setActionBarOverflowColor(presenter, mItemColor);
            }
        }
        return menu;
    }

    @Override
    public void setNavigationIcon(Drawable icon) {
        if (icon != null && mItemColor != 0) {
            icon.setColorFilter(mItemColor, PorterDuff.Mode.SRC_ATOP);
        }
        super.setNavigationIcon(icon);
    }

    public void setItemColor(int itemColor) {
        mItemColor = itemColor;
        setNavigationIcon(getNavigationIcon());
    }
}
