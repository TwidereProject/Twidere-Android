package org.mariotaku.twidere.util.media.preview.provider;

import org.junit.Test;
import org.mariotaku.twidere.model.ParcelableMedia;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by mariotaku on 16/2/20.
 */
public class InstagramProviderTest {

    final InstagramProvider provider = new InstagramProvider();

    @Test
    public void testFrom() throws Exception {
        ParcelableMedia media = provider.from("https://www.instagram.com/p/abcd1234");
        assertNotNull(media);
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media.media_url);
        media = provider.from("https://www.instagram.com/p/abcd1234/");
        assertNotNull(media);
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media.media_url);
        media = provider.from("https://www.instagram.com/p/abcd1234?key=value");
        assertNotNull(media);
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media.media_url);
        media = provider.from("https://www.instagram.com/p/abcd1234/?key=value");
        assertNotNull(media);
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media.media_url);
        media = provider.from("https://www.instagram.com/p/abcd1234/?key=value#fragment");
        assertNotNull(media);
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media.media_url);
        media = provider.from("https://www.instagram.com/p/abcd1234#fragment");
        assertNotNull(media);
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media.media_url);
        media = provider.from("https://www.instagram.com/p/abcd1234/#fragment");
        assertNotNull(media);
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media.media_url);
        media = provider.from("https://www.instagram.com/user");
        assertNull(media);
    }

}