package org.mariotaku.twidere.util;

import java.util.Collection;

/**
 * Created by mariotaku on 16/3/7.
 */
public class TwidereCollectionUtils {
    private TwidereCollectionUtils() {
    }

    public static String[] toStringArray(final Collection<?> list) {
        if (list == null) return null;
        final int length = list.size();
        final String[] stringArray = new String[length];
        int idx = 0;
        for (Object o : list) {
            stringArray[idx++] = ParseUtils.parseString(o);
        }
        return stringArray;
    }
}
