package org.mariotaku.twidere.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by mariotaku on 16/2/15.
 */
public class TwidereTypeUtils {

    private TwidereTypeUtils() {
    }

    public static String toSimpleName(Type type) {
        final StringBuilder sb = new StringBuilder();
        buildSimpleName(type, sb);
        return sb.toString();
    }

    private static void buildSimpleName(Type type, StringBuilder sb) {
        if (type instanceof Class) {
            sb.append(((Class) type).getSimpleName());
        } else if (type instanceof ParameterizedType) {
            buildSimpleName(((ParameterizedType) type).getRawType(), sb);
            sb.append("<");
            final Type[] args = ((ParameterizedType) type).getActualTypeArguments();
            for (int i = 0; i < args.length; i++) {
                if (i != 0) {
                    sb.append(",");
                }
                buildSimpleName(args[i], sb);
            }
            sb.append(">");
        }
    }
}
