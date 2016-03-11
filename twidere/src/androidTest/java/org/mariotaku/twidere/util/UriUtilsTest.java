package org.mariotaku.twidere.util;


import org.junit.Test;
import org.mariotaku.twidere.util.UriUtils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Created by mariotaku on 16/2/9.
 */
public class UriUtilsTest {

    @Test
    public void testGetAuthority() throws Exception {
        assertEquals("www.google.com", UriUtils.getAuthority("http://www.google.com/"));
        assertEquals("twitter.com", UriUtils.getAuthority("https://twitter.com"));
        assertNull(UriUtils.getAuthority("www.google.com/"));
    }

    @Test
    public void testGetPath() throws Exception {
        assertEquals("/", UriUtils.getPath("http://www.example.com/"));
        assertEquals("", UriUtils.getPath("http://www.example.com"));
        assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path"));
        assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path?with=query"));
        assertEquals("/test/path/", UriUtils.getPath("https://example.com/test/path/?with=query"));
        assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path?with=query#fragment"));
        assertEquals("/test/path/", UriUtils.getPath("https://example.com/test/path/?with=query#fragment"));
        assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path#fragment"));
        assertEquals("/test/path/", UriUtils.getPath("https://example.com/test/path/#fragment"));
    }
}