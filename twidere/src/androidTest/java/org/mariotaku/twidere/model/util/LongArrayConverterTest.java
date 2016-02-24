package org.mariotaku.twidere.model.util;

import android.database.MatrixCursor;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by mariotaku on 16/2/24.
 */
public class LongArrayConverterTest {
    private final LongArrayConverter converter = new LongArrayConverter();

    @Test
    public void testParseField() throws Exception {
        MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c", "d"});
        cursor.addRow(new String[]{"1,2,3,4", "5,6,7", "8,", ""});
        cursor.moveToFirst();
        assertArrayEquals(new long[]{1, 2, 3, 4}, converter.parseField(cursor, 0, TypeUtils.parameterize(long[].class)));
        assertArrayEquals(new long[]{5, 6, 7}, converter.parseField(cursor, 1, TypeUtils.parameterize(long[].class)));
        assertNull(converter.parseField(cursor, 2, TypeUtils.parameterize(long[].class)));
        assertNull(converter.parseField(cursor, 3, TypeUtils.parameterize(long[].class)));
    }

    @Test
    public void testWriteField() throws Exception {

    }
}