package org.mariotaku.twidere.util

import okhttp3.HttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Created by mariotaku on 16/2/10.
 */
class MicroBlogAPIFactoryTest {

    @Test
    @Throws(Exception::class)
    fun testGetApiUrl() {
        assertEquals("https://api.twitter.com/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", null))
        assertEquals("https://api.twitter.com/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", null))
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "/1.1/"))
        assertEquals("https://api.twitter.com/1.1", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "1.1"))
        assertEquals("https://api.twitter.com/1.1", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "/1.1"))
        assertEquals("https://api.twitter.com/1.1", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "/1.1"))
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "1.1/"))
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "1.1/"))
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "/1.1/"))
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "/1.1/"))
    }

    @Test
    fun testGetApiBaseUrl() {
        assertEquals("https://media.twitter.com", MicroBlogAPIFactory.getApiBaseUrl("https://api.twitter.com", "media"))
        assertNotNull(HttpUrl.parse(MicroBlogAPIFactory.getApiBaseUrl("https://[invalid]twitter.com/", "api")))
    }
}