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
import java.util.*

/**
 * Created by mariotaku on 2017/8/19.
 */
class CronExpressionTest {

    @Test
    fun testMatches() {
        // @daily (0:00 every day)
        val cal0h0m = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
        }
        // Every 1:00, 15th day of month
        val cal15dom1h0m = Calendar.getInstance(TimeZone.getTimeZone("JST")).apply {
            set(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.DAY_OF_MONTH, 15)
        }
        Assert.assertTrue(CronExpression.valueOf("0 0 * * *").matches(cal0h0m))
        Assert.assertFalse(CronExpression.valueOf("0 0 * * *").matches(cal15dom1h0m))

        // Here comes the timezone related part
        Assert.assertTrue(CronExpression.valueOf("0 1 15 * *").matches(cal15dom1h0m))
    }

    class FieldTest {
        @Test
        fun testParseBasic() {
            Assert.assertSame(CronExpression.AnyField.INSTANCE, CronExpression.FieldType.MINUTE.parseField("*"))
            Assert.assertArrayEquals(arrayOf(CronExpression.Range.single(0), CronExpression.Range.single(1)),
                    (CronExpression.FieldType.DAY_OF_WEEK.parseField("SUN,MON") as CronExpression.BasicField).ranges)
            Assert.assertArrayEquals(arrayOf(CronExpression.Range.single(0), CronExpression.Range(1, 5)),
                    (CronExpression.FieldType.DAY_OF_WEEK.parseField("SUN,MON-FRI") as CronExpression.BasicField).ranges)
        }
    }

    class RangeTest {

        @Test
        fun testParse() {
            Assert.assertEquals(CronExpression.Range(0, 6), CronExpression.Range.parse("0-6", CronExpression.Range(0, 10), null))
            Assert.assertEquals(CronExpression.Range.single(0), CronExpression.Range.parse("SUN", CronExpression.Range(0, 7), CronExpression.FieldType.DAY_OF_WEEK.textRepresentations))
            Assert.assertEquals(CronExpression.Range(0, 3), CronExpression.Range.parse("SUN-WED", CronExpression.Range(0, 7), CronExpression.FieldType.DAY_OF_WEEK.textRepresentations))
        }
    }
}