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

import com.twitter.Validator
import org.junit.Assert
import org.junit.Test

/**
 * Created by mariotaku on 2017/3/31.
 */
class ValidatorExtensionsKtTest {

    val validator = Validator()

    @Test
    fun getTweetLength() {
        Assert.assertEquals(0, validator.getTweetLength("@user1 ", true))
        Assert.assertEquals(0, validator.getTweetLength("@user1          ", true))
        Assert.assertEquals(4, validator.getTweetLength("@user1 @user2 test", true))
        Assert.assertEquals(4, validator.getTweetLength("@user1    @user2 test", true))
        Assert.assertEquals(4, validator.getTweetLength("@user1    @user2     test", true))
        Assert.assertEquals(9, validator.getTweetLength("@user1    @user2     test     ", true))
        Assert.assertEquals(11, validator.getTweetLength("@user1 test @user2", true))
        Assert.assertEquals(4, validator.getTweetLength("test", true))

        val long50Mentions = Array(50) { "@user${it + 1}" }.joinToString(" ")
        Assert.assertEquals(4, validator.getTweetLength("$long50Mentions test", true))
        Assert.assertEquals(12, validator.getTweetLength("$long50Mentions @user51 test", true))
    }

}