package org.mariotaku.twidere.provider

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by mariotaku on 2016/12/7.
 */

@RunWith(AndroidJUnit4::class)
class TwidereDataStoreTest {
    @Test
    fun testBaseUris() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        Assert.assertEquals(TwidereDataStore.BASE_CONTENT_URI, Uri.parse("content://twidere"))
        Assert.assertNull(resolver.query(TwidereDataStore.CONTENT_URI_NULL, null, null, null, null))
        Assert.assertNotNull(resolver.query(TwidereDataStore.CONTENT_URI_EMPTY, null, null, null, null))
    }
}
