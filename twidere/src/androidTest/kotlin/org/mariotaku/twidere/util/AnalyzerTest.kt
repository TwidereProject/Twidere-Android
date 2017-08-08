package org.mariotaku.twidere.util

import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.twidere.BuildConfig

/**
 * Created by mariotaku on 2016/12/15.
 */
@RunWith(AndroidJUnit4::class)
class AnalyzerTest {
    @Test
    fun testGetInstance() {
        if (BuildConfig.FLAVOR.contains("google")) {
            Assert.assertNotNull(Analyzer.implementation)
        }
    }
}