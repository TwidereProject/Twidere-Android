package org.mariotaku.twidere.model.util

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.test.R
import org.mariotaku.twidere.util.JsonSerializer

/**
 * Created by mariotaku on 2017/1/4.
 */

@RunWith(AndroidJUnit4::class)
class ParcelableStatusUtilsTest {

    val expectedStatusText = "Yalp Store (Download apks from Google Play Store). !gnusocial\n\nhttps://f-droid.org/app/com.github.yeriomin.yalpstore"

    @Test
    fun testFromStatus() {
        val context = InstrumentationRegistry.getInstrumentation().context
        val status_8754050 = context.resources.openRawResource(R.raw.status_8754050).use {
            val status = JsonSerializer.parse(it, Status::class.java)
            return@use status.toParcelable(UserKey("1234567", "gnusocial.de"), AccountType.STATUSNET)
        }

        val status_9171447 = context.resources.openRawResource(R.raw.status_9171447).use {
            val status = JsonSerializer.parse(it, Status::class.java)
            return@use status.toParcelable(UserKey("1234567", "gnusocial.de"), AccountType.STATUSNET)
        }

        Assert.assertEquals(status_8754050.text_unescaped, expectedStatusText)
        Assert.assertEquals(status_9171447.text_unescaped, expectedStatusText)
    }
}
