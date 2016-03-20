package org.mariotaku.twidere.util;


import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 * Created by mariotaku on 16/1/31.
 */
public class TwidereArrayUtilsTest {

    @Test
    public void testMergeArray() throws Exception {
        String[] array1 = {"1", "2"};
        String[] array2 = {"1", "2"};
        String[] array3 = null;

        String[] merged = new String[TwidereArrayUtils.arraysLength(array1, array2, array3)];
        TwidereArrayUtils.mergeArray(merged, array1, array2, array3);
        String[] expected = {"1", "2", "1", "2"};
        assertArrayEquals(expected, merged);
    }

    @Test
    public void testArraysLength() throws Exception {
        String[] array1 = {"1", "2"};
        String[] array2 = {"1", "2"};
        String[] array3 = null;
        assertEquals(4, TwidereArrayUtils.arraysLength(array1, array2, array3));
        assertEquals(6, TwidereArrayUtils.arraysLength(array1, array2, array2));
    }
}