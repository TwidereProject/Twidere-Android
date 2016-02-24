package org.mariotaku.twidere;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by mariotaku on 16/2/24.
 */
public class TwidereTest {

    @Test
    public void testCheckPermissionRequirement() {
        assertEquals(Twidere.Permission.GRANTED, Twidere.checkPermissionRequirement(new String[]{"a", "b", "c"}, new String[]{"a", "b", "c"}));
        assertEquals(Twidere.Permission.GRANTED, Twidere.checkPermissionRequirement(new String[]{"a", "b"}, new String[]{"a", "b", "c"}));
        assertEquals(Twidere.Permission.NONE, Twidere.checkPermissionRequirement(new String[]{"a", "b"}, new String[]{"a", "c"}));
        assertEquals(Twidere.Permission.DENIED, Twidere.checkPermissionRequirement(new String[]{"a", "b"}, new String[]{"denied"}));
    }
}