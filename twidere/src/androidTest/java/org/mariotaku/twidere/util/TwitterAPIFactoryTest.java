package org.mariotaku.twidere.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by mariotaku on 16/2/10.
 */
public class TwitterAPIFactoryTest {

    @Test
    public void testGetApiUrl() throws Exception {
        assertEquals("https://api.twitter.com/", TwitterAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", null));
        assertEquals("https://api.twitter.com/", TwitterAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", null));
        assertEquals("https://api.twitter.com/1.1/", TwitterAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "1.1"));
        assertEquals("https://api.twitter.com/1.1/", TwitterAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "1.1"));
        assertEquals("https://api.twitter.com/1.1/", TwitterAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "/1.1"));
        assertEquals("https://api.twitter.com/1.1/", TwitterAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "/1.1"));
        assertEquals("https://api.twitter.com/1.1/", TwitterAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "1.1/"));
        assertEquals("https://api.twitter.com/1.1/", TwitterAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "1.1/"));
        assertEquals("https://api.twitter.com/1.1/", TwitterAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "/1.1/"));
        assertEquals("https://api.twitter.com/1.1/", TwitterAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "/1.1/"));
    }

    @Test
    public void testGetApiBaseUrl() {
        assertEquals("https://media.twitter.com", TwitterAPIFactory.getApiBaseUrl("https://api.twitter.com", "media"));
    }
}