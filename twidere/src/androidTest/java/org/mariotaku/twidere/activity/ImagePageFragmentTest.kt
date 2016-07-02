package org.mariotaku.twidere.activity

import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by mariotaku on 16/3/3.
 */
class ImagePageFragmentTest {

    @Test
    @Throws(Exception::class)
    fun testReplaceTwitterMediaUri() {
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:large",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.png:large")).toString())
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:orig",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.png:orig")).toString())
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:large",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.jpg:large")).toString())
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:large",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.jpg:orig")).toString())
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.jpg")).toString())
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.jpg:")).toString())
        assertEquals("https://example.com/media/DEADBEEF.jpg",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://example.com/media/DEADBEEF.jpg")).toString())
    }
}