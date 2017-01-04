package org.mariotaku.twidere.model.util

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.bluelinelabs.logansquare.LoganSquare
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.test.R

/**
 * Created by mariotaku on 2017/1/4.
 */

@RunWith(AndroidJUnit4::class)
class ParcelableStatusUtilsTest {

    val expectedStatusText = "Yalp Store (Download apks from Google Play Store). !gnusocial\n\nhttps://f-droid.org/app/com.github.yeriomin.yalpstore"

    @Test
    fun testFromStatus() {
        val context = InstrumentationRegistry.getContext()
        val status_8754050 = context.resources.openRawResource(R.raw.status_8754050).use {
            val status = LoganSquare.parse(it, Status::class.java)
            return@use ParcelableStatusUtils.fromStatus(status, UserKey("1234567", "gnusocial.de"), false)
        }

        val status_9171447 = context.resources.openRawResource(R.raw.status_9171447).use {
            val status = LoganSquare.parse(it, Status::class.java)
            return@use ParcelableStatusUtils.fromStatus(status, UserKey("1234567", "gnusocial.de"), false)
        }

        Assert.assertEquals(status_8754050.text_unescaped, expectedStatusText)
        Assert.assertEquals(status_9171447.text_unescaped, expectedStatusText)
    }
}
