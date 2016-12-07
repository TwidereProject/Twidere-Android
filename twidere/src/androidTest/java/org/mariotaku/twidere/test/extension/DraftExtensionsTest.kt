package org.mariotaku.twidere.test.extension

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.twidere.extension.readMimeMessageFrom
import org.mariotaku.twidere.extension.writeMimeMessageTo
import org.mariotaku.twidere.model.Draft
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableMediaUpdate
import org.mariotaku.twidere.model.UserKey
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
        draft.timestamp = System.currentTimeMillis()
        draft.account_keys = arrayOf(UserKey("user1", "twitter.com"), UserKey("user2", "twitter.com"))
        draft.text = "Hello world"
        draft.media = arrayOf(ParcelableMediaUpdate().apply {
            this.uri = "file:///system/media/audio/ringtones/Atria.ogg"
            this.type = ParcelableMedia.Type.VIDEO
            this.alt_text = String(CharArray(420).apply {
                fill('A')
            })
        })
        val output = ByteArrayOutputStream()
        draft.writeMimeMessageTo(context, output)
        val input = ByteArrayInputStream(output.toByteArray())

        val newDraft = Draft()
        newDraft.readMimeMessageFrom(context, input)

        Assert.assertArrayEquals(draft.account_keys?.sortedArray(), newDraft.account_keys?.sortedArray())
        Assert.assertEquals(TimeUnit.MILLISECONDS.toSeconds(draft.timestamp), TimeUnit.MILLISECONDS.toSeconds(newDraft.timestamp))
        Assert.assertEquals(draft.text, newDraft.text)
        draft.media?.forEachIndexed { idx, expected ->
            val actual = newDraft.media!![idx]
            Assert.assertEquals(expected.alt_text, actual.alt_text)
            Assert.assertEquals(expected.type, actual.type)
        }
    }
}