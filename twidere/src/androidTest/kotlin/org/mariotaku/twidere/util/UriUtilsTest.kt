package org.mariotaku.twidere.util


import org.junit.Assert
import org.junit.Test


/**
 * Created by mariotaku on 16/2/9.
 */
class UriUtilsTest {

    @Test
    @Throws(Exception::class)
    fun testGetAuthority() {
        Assert.assertEquals("www.google.com", UriUtils.getAuthority("http://www.google.com/"))
        Assert.assertEquals("twitter.com", UriUtils.getAuthority("https://twitter.com"))
        Assert.assertNull(UriUtils.getAuthority("www.google.com/"))
    }

    @Test
    @Throws(Exception::class)
    fun testGetPath() {
        Assert.assertEquals("/", UriUtils.getPath("http://www.example.com/"))
        Assert.assertEquals("", UriUtils.getPath("http://www.example.com"))
        Assert.assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path"))
        Assert.assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path?with=query"))
        Assert.assertEquals("/test/path/", UriUtils.getPath("https://example.com/test/path/?with=query"))
        Assert.assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path?with=query#fragment"))
        Assert.assertEquals("/test/path/", UriUtils.getPath("https://example.com/test/path/?with=query#fragment"))
        Assert.assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path#fragment"))
        Assert.assertEquals("/test/path/", UriUtils.getPath("https://example.com/test/path/#fragment"))
    }
}