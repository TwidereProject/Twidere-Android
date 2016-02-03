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

package org.mariotaku.twidere.adapter.iface;

import android.support.annotation.IntDef;

/**
 * Created by mariotaku on 15/4/16.
 */
public interface ILoadMoreSupportAdapter {
    int ITEM_VIEW_TYPE_LOAD_INDICATOR = 0;

    @IndicatorPosition
    int getLoadMoreIndicatorPosition();

    void setLoadMoreIndicatorPosition(@IndicatorPosition int position);

    @IndicatorPosition
    int getLoadMoreSupportedPosition();

    void setLoadMoreSupportedPosition(@IndicatorPosition int supported);

    @IntDef(flag = true, value = {IndicatorPosition.NONE, IndicatorPosition.START,
            IndicatorPosition.END, IndicatorPosition.BOTH})
    @interface IndicatorPosition {
        int NONE = 0;
        int START = 0b01;
        int END = 0b10;
        int BOTH = START | END;
    }

    class IndicatorPositionUtils {
        @IndicatorPosition
        public static int apply(@IndicatorPosition int orig, @IndicatorPosition int supported) {
            return orig & supported;
        }

        @IndicatorPosition
        public static boolean has(@IndicatorPosition int flags, @IndicatorPosition int compare) {
            return (flags & compare) != 0;
        }
    }
}
