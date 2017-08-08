package org.mariotaku.twidere.util

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by mariotaku on 2016/12/7.
 */
@RunWith(AndroidJUnit4::class)
class DataStoreUtilsTest {
    @Test
    fun testCleanDatabasesByItemLimit() {
        val context = InstrumentationRegistry.getTargetContext()
        DataStoreUtils.cleanDatabasesByItemLimit(context)
    }

    @Test
    fun testGetAccountKeys() {
        val context = InstrumentationRegistry.getTargetContext()
        DataStoreUtils.getAccountKeys(context)
    }
}