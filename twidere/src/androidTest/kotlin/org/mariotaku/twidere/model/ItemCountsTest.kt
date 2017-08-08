/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model

import org.junit.Assert
import org.junit.Test

/**
 * Created by mariotaku on 2017/3/13.
 */
class ItemCountsTest {
    @Test
    fun getItemCountIndex() {
        val counts = ItemCounts(3)
        counts[0] = 2
        counts[1] = 3
        counts[2] = 3

        Assert.assertEquals(0, counts.getItemCountIndex(1))
        Assert.assertEquals(1, counts.getItemCountIndex(2))
        Assert.assertEquals(1, counts.getItemCountIndex(4))
        Assert.assertEquals(2, counts.getItemCountIndex(7))
        Assert.assertEquals(-1, counts.getItemCountIndex(10))
        Assert.assertEquals(-1, counts.getItemCountIndex(-1))
    }

}