package org.mariotaku.twidere.util;

import java.util.Locale;

/**
 * Created by mariotaku on 16/3/8.
 */
public class InternalParseUtils {
    private InternalParseUtils() {
    }

    public static String parsePrettyDecimal(double num, int decimalDigits) {
        String result = String.format(Locale.US, "%." + decimalDigits + "f", num);
        int dotIdx = result.lastIndexOf('.');
        if (dotIdx == -1) return result;
        int i;
        for (i = result.length() - 1; i >= 0; i--) {
            if (result.charAt(i) != '0') break;
        }
        return result.substring(0, i == dotIdx ? dotIdx : i + 1);
    }

}
