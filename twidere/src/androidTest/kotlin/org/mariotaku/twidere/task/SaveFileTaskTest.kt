package org.mariotaku.twidere.task

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by mariotaku on 16/3/2.
 */
class SaveFileTaskTest {

    @Test
    @Throws(Exception::class)
    fun testGetFileNameWithExtension() {
        assertEquals("abcdefghijklmn.jpg",
                SaveFileTask.getFileNameWithExtension("pbs_twimg_com_media_abcdefghijklmn_jpg",
                        "jpg", '_', null))
        assertEquals("pbs_twimg_com_media_abcdefghijklmn_jpg",
                SaveFileTask.getFileNameWithExtension("pbs_twimg_com_media_abcdefghijklmn_jpg",
                        null, '_', null))
        assertEquals("pbs_twimg_com_media_abcdefghijklmn_jpgsuffix",
                SaveFileTask.getFileNameWithExtension("pbs_twimg_com_media_abcdefghijklmn_jpg",
                        null, '_', "suffix"))
    }
}