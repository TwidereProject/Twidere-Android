package org.mariotaku.twidere.util.media.preview.provider

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.twidere.model.ParcelableMedia

/**
 * Created by mariotaku on 16/2/20.
 */
@RunWith(AndroidJUnit4::class)
class InstagramProviderTest {

    internal val provider = InstagramProvider()

    @Test
    fun testFrom() {
        var media: ParcelableMedia? = provider.from("https://www.instagram.com/p/abcd1234")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/p/abcd1234/")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/p/abcd1234?key=value")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/p/abcd1234/?key=value")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/p/abcd1234/?key=value#fragment")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/p/abcd1234#fragment")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/p/abcd1234/#fragment")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/user")
        assertNull(media)
    }

}