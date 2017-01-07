package org.mariotaku.ktextension

import android.graphics.Color
import org.junit.Assert
import org.junit.Test

/**
 * Created by mariotaku on 2017/1/8.
 */
class ColorExtensionsTest {
    @Test
    fun testToHexColor() {
        Assert.assertEquals("#FFFF0000", toHexColor(Color.RED, format = HexColorFormat.ARGB))
        Assert.assertEquals("#FF0000", toHexColor(Color.RED, format = HexColorFormat.RGB))
        Assert.assertEquals(Color.RED, Color.parseColor(toHexColor(Color.RED)))
    }

}