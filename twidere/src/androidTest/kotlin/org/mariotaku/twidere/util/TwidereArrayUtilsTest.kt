package org.mariotaku.twidere.util


import org.junit.Assert
import org.junit.Test

/**
 * Created by mariotaku on 16/1/31.
 */
class TwidereArrayUtilsTest {

    @Test
    @Throws(Exception::class)
    fun testMergeArray() {
        val array1 = arrayOf("1", "2")
        val array2 = arrayOf("1", "2")
        val array3: Array<String>? = null

        //noinspection ConstantConditions
        val merged = arrayOfNulls<String>(TwidereArrayUtils.arraysLength(array1, array2, array3))
        //noinspection ConstantConditions
        TwidereArrayUtils.mergeArray(merged, array1, array2, array3)
        val expected = arrayOf("1", "2", "1", "2")
        Assert.assertArrayEquals(expected, merged)
    }

    @Test
    @Throws(Exception::class)
    fun testArraysLength() {
        val array1 = arrayOf("1", "2")
        val array2 = arrayOf("1", "2")
        val array3: Array<String>? = null
        //noinspection ConstantConditions
        Assert.assertEquals(4, TwidereArrayUtils.arraysLength(array1, array2, array3))
        Assert.assertEquals(6, TwidereArrayUtils.arraysLength(array1, array2, array2))
    }
}