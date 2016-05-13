package org.mariotaku.twidere.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by mariotaku on 16/2/10.
 */
public class MicroBlogAPIFactoryTest {

    @Test
    public void testGetApiUrl() throws Exception {
        assertEquals("https://api.twitter.com/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", null));
        assertEquals("https://api.twitter.com/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", null));
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "/1.1/"));
        assertEquals("https://api.twitter.com/1.1", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "1.1"));
        assertEquals("https://api.twitter.com/1.1", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "/1.1"));
        assertEquals("https://api.twitter.com/1.1", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "/1.1"));
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "1.1/"));
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "1.1/"));
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "/1.1/"));
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "/1.1/"));
    }

    @Test
    public void testGetApiBaseUrl() {
        assertEquals("https://media.twitter.com", MicroBlogAPIFactory.getApiBaseUrl("https://api.twitter.com", "media"));
    }
}