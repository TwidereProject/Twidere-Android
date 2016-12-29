package org.mariotaku.twidere.extension.model

import android.net.Uri
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.apache.commons.io.IOUtils
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.twidere.model.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Created by mariotaku on 2016/12/7.
 */
@RunWith(AndroidJUnit4::class)
class DraftExtensionsTest {
    @Test
    fun testMimeMessageProcessing() {
        val context = InstrumentationRegistry.getTargetContext()
        val draft = Draft()
        draft.action_type = Draft.Action.UPDATE_STATUS
        draft.timestamp = System.currentTimeMillis()
        draft.account_keys = arrayOf(UserKey("user1", "twitter.com"), UserKey("user2", "twitter.com"))
        draft.text = "Hello world 测试"
        draft.location = ParcelableLocation(-11.956, 99.625) // Randomly generated
        draft.media = arrayOf(
                "file:///system/media/audio/ringtones/Atria.ogg",
                "file:///system/media/audio/ringtones/Callisto.ogg",
                "file:///system/media/audio/ringtones/Dione.ogg"
        ).map { uri ->
            ParcelableMediaUpdate().apply {
                this.uri = uri
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
            Assert.assertTrue(IOUtils.contentEquals(stl, str))
        }
    }
}