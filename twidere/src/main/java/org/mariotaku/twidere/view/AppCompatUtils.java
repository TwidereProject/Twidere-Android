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

import android.support.v7.app.ActionBar;
import android.support.v7.internal.app.ToolbarActionBar;
import android.support.v7.internal.widget.DecorToolbar;
import android.support.v7.widget.Toolbar;

import org.mariotaku.twidere.util.Utils;

/**
 * Created by mariotaku on 15/10/12.
 */
public class AppCompatUtils {

    public static Toolbar findToolbarForActionBar(ActionBar actionBar) {
        if (actionBar instanceof ToolbarActionBar) {
            final Object decorToolbar = Utils.findFieldOfTypes(actionBar, ToolbarActionBar.class, DecorToolbar.class);
            if (decorToolbar instanceof DecorToolbar) {
                return (Toolbar) ((DecorToolbar) decorToolbar).getViewGroup();
            }
        }
        return null;
    }

}
