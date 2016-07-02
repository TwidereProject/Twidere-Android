package org.mariotaku.twidere.util

import org.apache.commons.lang3.reflect.TypeUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mariotaku.microblog.library.twitter.model.CursorTimestampResponse
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.Status

/**
 * Created by mariotaku on 16/2/15.
 */
class TwidereTypeUtilsTest {

    @Test
    @Throws(Exception::class)
    fun testGetSimpleName() {
        assertEquals("CursorTimestampResponse", TwidereTypeUtils.toSimpleName(CursorTimestampResponse::class.java))
        assertEquals("ResponseList<Status>", TwidereTypeUtils.toSimpleName(TypeUtils.parameterize(ResponseList::class.java, Status::class.java)))
        assertEquals("List<List<Object>>", TwidereTypeUtils.toSimpleName(TypeUtils.parameterize(List::class.java, TypeUtils.parameterize(List::class.java, Any::class.java))))
    }
}