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

package org.mariotaku.ktextension

import nl.komponents.kovenant.task
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit

class PromiseExtensionsKtTest {
    @Test
    fun testDeadlineTriggered() {
        try {
            task {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5))
            }.deadline(1, TimeUnit.SECONDS).get()
            Assert.fail()
        } catch (expected: DeadlineException) {
            // Normal!
        }
    }

    @Test
    fun testDeadlineNotTriggered() {
        try {
            val result = task {
                Thread.sleep(TimeUnit.SECONDS.toMillis(1))
                return@task true
            }.deadline(3, TimeUnit.SECONDS).get()
            Assert.assertTrue(result)
        } catch (e: DeadlineException) {
            Assert.fail()
        }
    }

}