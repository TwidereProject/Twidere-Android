package org.mariotaku.twidere.model

import org.junit.Test

import org.junit.Assert.assertEquals

/**
 * Created by mariotaku on 16/3/9.
 */
class UserKeyTest {

    @Test
    @Throws(Exception::class)
    fun testToString() {
        assertEquals("abc@twitter.com", UserKey("abc", "twitter.com").toString())
        assertEquals("\\@user@twitter.com", UserKey("@user", "twitter.com").toString())
        assertEquals("\\@u\\\\ser@twitter.com", UserKey("@u\\ser", "twitter.com").toString())
    }

    @Test
    @Throws(Exception::class)
    fun testValueOf() {
        assertEquals(UserKey("abc", "twitter.com"), UserKey.valueOf("abc@twitter.com"))
        assertEquals(UserKey("abc@", "twitter.com"), UserKey.valueOf("abc\\@@twitter.com"))
        assertEquals(UserKey("abc@", "twitter.com"), UserKey.valueOf("a\\bc\\@@twitter.com"))
        assertEquals(UserKey("a\\bc@", "twitter.com"), UserKey.valueOf("a\\\\bc\\@@twitter.com"))
    }
}