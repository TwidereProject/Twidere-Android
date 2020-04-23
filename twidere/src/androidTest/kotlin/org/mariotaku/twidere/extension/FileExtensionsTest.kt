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

package org.mariotaku.twidere.extension

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Created by mariotaku on 2017/1/24.
 */
@RunWith(AndroidJUnit4::class)
class FileExtensionsTest {
    @Test
    fun testTempFileInputStream() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val random = Random()
        val testData = ByteArray(1024)
        random.nextBytes(testData)
        val compareData = context.cacheDir.tempInputStream { os ->
            os.write(testData)
        }.use { it.readBytes() }
        Assert.assertArrayEquals(testData, compareData)
    }

}