package org.mariotaku.twidere.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by mariotaku on 16/1/23.
 */
public class TwidereMathUtilsTest {

    public void testClamp() throws Exception {

    }

    public void testClamp1() throws Exception {

    }

    public void testNextPowerOf2() throws Exception {

    }

    public void testPrevPowerOf2() throws Exception {

    }

    public void testSum() throws Exception {

    }

    public void testSum1() throws Exception {

    }

    public void testSum2() throws Exception {

    }

    @Test
    public void testInRange() {
        assertTrue(TwidereMathUtils.inRange(5, 0, 10, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(0, 0, 10, TwidereMathUtils.RANGE_EXCLUSIVE_EXCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(0, 5, 10, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(5, 5, 10, TwidereMathUtils.RANGE_EXCLUSIVE_INCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(10, 5, 10, TwidereMathUtils.RANGE_INCLUSIVE_EXCLUSIVE));
    }

    @Test
    public void testInRange1() {
        assertTrue(TwidereMathUtils.inRange(5f, 0f, 10f, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(0f, 0f, 10f, TwidereMathUtils.RANGE_EXCLUSIVE_EXCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(0f, 5f, 10f, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(5f, 5f, 10f, TwidereMathUtils.RANGE_EXCLUSIVE_INCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(10f, 5f, 10f, TwidereMathUtils.RANGE_INCLUSIVE_EXCLUSIVE));
    }
}