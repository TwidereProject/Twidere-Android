package org.mariotaku.twidere.activity;

import android.net.Uri;

import org.junit.Test;
import org.mariotaku.twidere.activity.MediaViewerActivity;

import static org.junit.Assert.assertEquals;

/**
 * Created by mariotaku on 16/3/3.
 */
public class ImagePageFragmentTest {

    @Test
    public void testReplaceTwitterMediaUri() throws Exception {
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:large",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.png:large")).toString());
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:orig",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.png:orig")).toString());
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:large",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.jpg:large")).toString());
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:large",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.jpg:orig")).toString());
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.jpg")).toString());
        assertEquals("https://pbs.twimg.com/media/DEADBEEF.png:",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://pbs.twimg.com/media/DEADBEEF.jpg:")).toString());
        assertEquals("https://example.com/media/DEADBEEF.jpg",
                MediaViewerActivity.ImagePageFragment.replaceTwitterMediaUri(Uri.parse(
                        "https://example.com/media/DEADBEEF.jpg")).toString());
    }
}