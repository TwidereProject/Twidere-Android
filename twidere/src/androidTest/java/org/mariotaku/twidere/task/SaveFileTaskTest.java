package org.mariotaku.twidere.task;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by mariotaku on 16/3/2.
 */
public class SaveFileTaskTest {

    @Test
    public void testGetFileNameWithExtension() throws Exception {
        assertEquals("pbs_twimg_com_media_abcdefghijklmn.jpg",
                SaveFileTask.getFileNameWithExtension("pbs_twimg_com_media_abcdefghijklmn_jpg",
                        "jpg", '_', null));
        assertEquals("pbs_twimg_com_media_abcdefghijklmn_jpg",
                SaveFileTask.getFileNameWithExtension("pbs_twimg_com_media_abcdefghijklmn_jpg",
                        null, '_', null));
        assertEquals("pbs_twimg_com_media_abcdefghijklmn_jpgsuffix",
                SaveFileTask.getFileNameWithExtension("pbs_twimg_com_media_abcdefghijklmn_jpg",
                        null, '_', "suffix"));
    }
}