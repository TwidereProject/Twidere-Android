package org.mariotaku.twidere.util

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Created by mariotaku on 16/3/26.
 */
class UnitConvertUtilsTest {

    @Test
    fun testCalculateProperCount() {
        assertEquals("201", UnitConvertUtils.calculateProperCount(201))
        assertEquals("2.2 K", UnitConvertUtils.calculateProperCount(2201))
        assertEquals("2.1 K", UnitConvertUtils.calculateProperCount(2100))
        assertEquals("2 K", UnitConvertUtils.calculateProperCount(2000))
        assertEquals("2 K", UnitConvertUtils.calculateProperCount(2049))
        assertEquals("2.1 K", UnitConvertUtils.calculateProperCount(2050))
        assertEquals("2.1 K", UnitConvertUtils.calculateProperCount(2099))
        assertEquals("2.4 K", UnitConvertUtils.calculateProperCount(2430))
        assertEquals("2.5 K", UnitConvertUtils.calculateProperCount(2499))
        assertEquals("2.4 K", UnitConvertUtils.calculateProperCount(2449))
        assertEquals("2.5 K", UnitConvertUtils.calculateProperCount(2450))
    }
}