package org.mariotaku.twidere.test.provider

import android.net.Uri
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.twidere.provider.TwidereDataStore

/**
 * Created by mariotaku on 2016/12/7.
 */

@RunWith(AndroidJUnit4::class)
class TwidereDataStoreTest {
    @Test
    fun testBaseUris() {
        val context = InstrumentationRegistry.getTargetContext()
        val resolver = context.contentResolver
        Assert.assertEquals(TwidereDataStore.BASE_CONTENT_URI, Uri.parse("content://twidere"))
        Assert.assertNull(resolver.query(TwidereDataStore.CONTENT_URI_NULL, null, null, null, null))
        Assert.assertNotNull(resolver.query(TwidereDataStore.CONTENT_URI_EMPTY, null, null, null, null))
    }
}
