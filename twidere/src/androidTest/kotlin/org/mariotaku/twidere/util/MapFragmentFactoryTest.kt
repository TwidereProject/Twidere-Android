package org.mariotaku.twidere.util

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by mariotaku on 2016/12/15.
 */
@RunWith(AndroidJUnit4::class)
class MapFragmentFactoryTest {
    @Test
    fun testGetInstance() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        MapFragmentFactory.instance.createMapFragment(context = context)
    }
}