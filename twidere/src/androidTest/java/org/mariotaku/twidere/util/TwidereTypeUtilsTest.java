package org.mariotaku.twidere.util;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Test;
import org.mariotaku.microblog.library.twitter.model.CursorTimestampResponse;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.Status;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by mariotaku on 16/2/15.
 */
public class TwidereTypeUtilsTest {

    @Test
    public void testGetSimpleName() throws Exception {
        assertEquals("CursorTimestampResponse", TwidereTypeUtils.toSimpleName(CursorTimestampResponse.class));
        assertEquals("ResponseList<Status>", TwidereTypeUtils.toSimpleName(TypeUtils.parameterize(ResponseList.class, Status.class)));
        assertEquals("List<List<Object>>", TwidereTypeUtils.toSimpleName(TypeUtils.parameterize(List.class, TypeUtils.parameterize(List.class, Object.class))));
    }
}