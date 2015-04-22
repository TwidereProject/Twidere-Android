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

import android.view.View;
import android.widget.ListView;

/**
 * Created by mariotaku on 15/4/22.
 */
public class ListViewUtils {

    public static int getFirstFullyVisiblePosition(final ListView listView) {
        final int firstVisiblePosition = listView.getFirstVisiblePosition();
        final View firstVisibleChild = listView.getChildAt(0);
        if (firstVisibleChild != null && firstVisibleChild.getTop() < 0
                && firstVisiblePosition + 1 < listView.getCount()) {
            return firstVisiblePosition + 1;
        }
        return firstVisiblePosition;
    }

}
