package org.mariotaku.twidere.util

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by mariotaku on 2016/12/7.
 */
@RunWith(AndroidJUnit4::class)
class DataStoreUtilsTest {
    @Test
    fun testCleanDatabasesByItemLimit() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        DataStoreUtils.cleanDatabasesByItemLimit(context)
    }

    @Test
    fun testGetAccountKeys() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        DataStoreUtils.getAccountKeys(context)
    }
}