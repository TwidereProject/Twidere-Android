package org.mariotaku.microblog.library.twitter.util;

/**
 * Created by mariotaku on 16/8/20.
 */
public class InternalArrayUtil {

    public static String join(Object[] array, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i != 0) {
                sb.append(separator);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }

}
