package org.mariotaku.twidere.util

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * Created by mariotaku on 2017/1/24.
 */
@RunWith(AndroidJUnit4::class)
class IOFunctionsKtTest {
    @Test
    fun testTempFileInputStream() {
        val context = InstrumentationRegistry.getTargetContext()
        val random = Random()
        val testData = ByteArray(1024)
        random.nextBytes(testData)
        val compareData = tempFileInputStream(context) { os ->
            os.write(testData)
        }.readBytes(1024)
        Assert.assertArrayEquals(testData, compareData)
    }

}