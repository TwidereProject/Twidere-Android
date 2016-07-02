package org.mariotaku.twidere.util


import org.junit.Test

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull

/**
 * Created by mariotaku on 16/2/9.
 */
class UriUtilsTest {

    @Test
    @Throws(Exception::class)
    fun testGetAuthority() {
        assertEquals("www.google.com", UriUtils.getAuthority("http://www.google.com/"))
        assertEquals("twitter.com", UriUtils.getAuthority("https://twitter.com"))
        assertNull(UriUtils.getAuthority("www.google.com/"))
    }

    @Test
    @Throws(Exception::class)
    fun testGetPath() {
        assertEquals("/", UriUtils.getPath("http://www.example.com/"))
        assertEquals("", UriUtils.getPath("http://www.example.com"))
        assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path"))
        assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path?with=query"))
        assertEquals("/test/path/", UriUtils.getPath("https://example.com/test/path/?with=query"))
        assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path?with=query#fragment"))
        assertEquals("/test/path/", UriUtils.getPath("https://example.com/test/path/?with=query#fragment"))
        assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path#fragment"))
        assertEquals("/test/path/", UriUtils.getPath("https://example.com/test/path/#fragment"))
    }
}