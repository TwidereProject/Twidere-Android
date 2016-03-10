package org.mariotaku.twidere.model;

import junit.framework.TestCase;

/**
 * Created by mariotaku on 16/3/10.
 */
public class UserKeyLocalTest extends TestCase {

    public void testToString() throws Exception {
        assertEquals("abc@twitter.com", new UserKey("abc", "twitter.com").toString());
        assertEquals("\\@user@twitter.com", new UserKey("@user", "twitter.com").toString());
        assertEquals("\\@u\\\\ser@twitter.com", new UserKey("@u\\ser", "twitter.com").toString());
    }

    public void testValueOf() throws Exception {
        assertEquals(new UserKey("abc", "twitter.com"), UserKey.valueOf("abc@twitter.com"));
        assertEquals(new UserKey("abc@", "twitter.com"), UserKey.valueOf("abc\\@@twitter.com"));
        assertEquals(new UserKey("abc@", "twitter.com"), UserKey.valueOf("a\\bc\\@@twitter.com"));
        assertEquals(new UserKey("a\\bc@", "twitter.com"), UserKey.valueOf("a\\\\bc\\@@twitter.com"));
        assertEquals(new UserKey("abc", "twitter.com"), UserKey.valueOf("abc@twitter.com,def@twitter.com"));
        assertEquals(new UserKey("@abc", "twitter.com"), UserKey.valueOf("\\@abc@twitter.com,def@twitter.com"));
    }
}