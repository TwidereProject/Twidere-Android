package org.mariotaku.twidere.util

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by mariotaku on 2016/12/15.
 */
@RunWith(AndroidJUnit4::class)
class MapFragmentFactoryTest {
    @Test
    fun testGetInstance() {
        val context = InstrumentationRegistry.getTargetContext()
        MapFragmentFactory.instance.createMapFragment(context = context)
    }
}