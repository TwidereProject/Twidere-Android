package org.mariotaku.twidere.activity

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mariotaku.twidere.fragment.ImagePageFragment

/**
 * Created by mariotaku on 16/3/3.
 */
class ImagePageFragmentTest {

    @Test
    @Throws(Exception::class)
    fun testReplaceTwitterMediaUri() {
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:large",
                ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.png:large")).toString())
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:orig",
                ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.png:orig")).toString())
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:large",
                ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.jpg:large")).toString())
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:large",
                ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.jpg:orig")).toString())
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png",
                ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.jpg")).toString())
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:",
                ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.jpg:")).toString())
        assertEquals("https://example.com/media/DEADBEEF.jpg",
                ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://example.com/media/DEADBEEF.jpg")).toString())
    }
}