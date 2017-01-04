package org.mariotaku.twidere.util

import org.junit.Test

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

/**
 * Created by mariotaku on 16/1/23.
 */
class TwidereMathUtilsTest {

    @Throws(Exception::class)
    fun testClamp() {

    }

    @Throws(Exception::class)
    fun testClamp1() {

    }

    @Throws(Exception::class)
    fun testNextPowerOf2() {

    }

    @Throws(Exception::class)
    fun testPrevPowerOf2() {

    }

    @Throws(Exception::class)
    fun testSum() {

    }

    @Throws(Exception::class)
    fun testSum1() {

    }

    @Throws(Exception::class)
    fun testSum2() {

    }

    @Test
    fun testInRange() {
        assertTrue(TwidereMathUtils.inRange(5, 0, 10, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(0, 0, 10, TwidereMathUtils.RANGE_EXCLUSIVE_EXCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(0, 5, 10, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(5, 5, 10, TwidereMathUtils.RANGE_EXCLUSIVE_INCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(10, 5, 10, TwidereMathUtils.RANGE_INCLUSIVE_EXCLUSIVE))
    }

    @Test
    fun testInRange1() {
        assertTrue(TwidereMathUtils.inRange(5f, 0f, 10f, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(0f, 0f, 10f, TwidereMathUtils.RANGE_EXCLUSIVE_EXCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(0f, 5f, 10f, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(5f, 5f, 10f, TwidereMathUtils.RANGE_EXCLUSIVE_INCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(10f, 5f, 10f, TwidereMathUtils.RANGE_INCLUSIVE_EXCLUSIVE))
    }
}