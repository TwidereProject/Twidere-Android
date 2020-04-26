package org.mariotaku.twidere.extension.model

import android.media.RingtoneManager
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.twidere.model.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * Created by mariotaku on 2016/12/7.
 */
@RunWith(AndroidJUnit4::class)
class DraftExtensionsTest {
    @Test
    fun testMimeMessageProcessing() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val draft = Draft()
        draft.action_type = Draft.Action.UPDATE_STATUS
        draft.timestamp = System.currentTimeMillis()
        draft.account_keys = arrayOf(UserKey("user1", "twitter.com"), UserKey("user2", "twitter.com"))
        draft.text = "Hello world 测试"
        draft.location = ParcelableLocation(-11.956, 99.625) // Randomly generated
        draft.media = arrayOf(
                RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE),
                RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION),
                RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        ).map { uri ->
            ParcelableMediaUpdate().apply {
                this.uri = uri.toString()
                this.type = ParcelableMedia.Type.VIDEO
                this.alt_text = String(CharArray(420).apply {
                    fill('A')
                })
            }
        }.toTypedArray()
        val output = ByteArrayOutputStream()
        draft.writeMimeMessageTo(context, output)
        val input = ByteArrayInputStream(output.toByteArray())

        val newDraft = Draft()
        newDraft.readMimeMessageFrom(context, input)

        Assert.assertArrayEquals(draft.account_keys?.sortedArray(), newDraft.account_keys?.sortedArray())
        Assert.assertEquals(TimeUnit.MILLISECONDS.toSeconds(draft.timestamp), TimeUnit.MILLISECONDS.toSeconds(newDraft.timestamp))
        Assert.assertEquals(draft.text, newDraft.text)
        Assert.assertEquals(draft.location, newDraft.location)
        Assert.assertEquals(draft.action_type, newDraft.action_type)
        Assert.assertEquals(draft.action_extras, newDraft.action_extras)
        draft.media?.forEachIndexed { idx, expected ->
            val actual = newDraft.media!![idx]
            Assert.assertEquals(expected.alt_text, actual.alt_text)
            Assert.assertEquals(expected.type, actual.type)
            val stl = context.contentResolver.openInputStream(Uri.parse(expected.uri))
            val str = context.contentResolver.openInputStream(Uri.parse(actual.uri))
            Assert.assertTrue(stl!!.contentEquals(str!!))
            stl.close()
            str.close()
        }
    }

}

private fun InputStream.contentEquals(that: InputStream): Boolean {
    var len1 = 0
    var len2 = 0
    val buf1 = ByteArray(8192)
    val buf2 = ByteArray(8192)
    while (len1 != -1 && len2 != -1) {
        len1 = this.read(buf1)
        len2 = that.read(buf2)
        if (!buf1.contentEquals(buf2)) {
            return false
        }
    }
    return len1 == len2
}
