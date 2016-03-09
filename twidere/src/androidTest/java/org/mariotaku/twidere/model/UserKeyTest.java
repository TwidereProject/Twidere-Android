package org.mariotaku.twidere.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by mariotaku on 16/3/9.
 */
public class UserKeyTest {

    @Test
    public void testToString() throws Exception {
        assertEquals("abc@twitter.com", new UserKey("abc", "twitter.com").toString());
    }

    @Test
    public void testValueOf() throws Exception {
        assertEquals(UserKey.valueOf("abc@twitter.com"), new UserKey("abc", "twitter.com"));
    }
}