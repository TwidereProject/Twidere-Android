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

package org.mariotaku.twidere.util

import okhttp3.HttpUrl
import org.junit.Assert
import org.junit.Test

/**
 * Created by mariotaku on 2017/4/9.
 */
class HttpClientFactoryTest {
    @Test
    fun testReplaceUrl() {
        val format1 = "https://proxy.com/[SCHEME]/[AUTHORITY]/[PATH][?QUERY][#FRAGMENT]"
        val format2 = "https://proxy.com/[AUTHORITY]/[PATH][?QUERY][#FRAGMENT]"
        val format3 = "https://proxy.com/[AUTHORITY][/PATH][?QUERY][#FRAGMENT]"
        val url1 = HttpUrl.parse("https://example.com:8080/path?query=value#fragment")!!
        val url2 = HttpUrl.parse("https://example.com:8080/path?query=value")!!
        val url3 = HttpUrl.parse("https://example.com:8080/path#fragment")!!
        val url4 = HttpUrl.parse("https://example.com:8080/path")!!
        val url5 = HttpUrl.parse("https://example.com/path")!!

        Assert.assertEquals("https://proxy.com/https/example.com%3A8080/path?query=value#fragment",
                HttpClientFactory.replaceUrl(url1, format1))
        Assert.assertEquals("https://proxy.com/example.com%3A8080/path?query=value#fragment",
                HttpClientFactory.replaceUrl(url1, format2))
        Assert.assertEquals("https://proxy.com/example.com%3A8080/path?query=value#fragment",
                HttpClientFactory.replaceUrl(url1, format3))
        Assert.assertEquals("https://proxy.com/https/example.com%3A8080/path?query=value",
                HttpClientFactory.replaceUrl(url2, format1))
        Assert.assertEquals("https://proxy.com/https/example.com%3A8080/path#fragment",
                HttpClientFactory.replaceUrl(url3, format1))
        Assert.assertEquals("https://proxy.com/https/example.com%3A8080/path",
                HttpClientFactory.replaceUrl(url4, format1))
        Assert.assertEquals("https://proxy.com/https/example.com/path",
                HttpClientFactory.replaceUrl(url5, format1))
    }

}