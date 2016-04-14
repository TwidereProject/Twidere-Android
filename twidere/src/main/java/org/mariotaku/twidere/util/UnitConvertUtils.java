package org.mariotaku.twidere.util;

import java.util.Locale;

/**
 * Created by mariotaku on 16/2/4.
 */
public class UnitConvertUtils {

    public static final String[] fileSizeUnits = {"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB",
            "YB"};

    public static final String[] countUnits = {null, "K", "M", "B"};

    private UnitConvertUtils() {
    }

    public static String calculateProperSize(double bytes) {
        double value = bytes;
        int index;
        for (index = 0; index < fileSizeUnits.length; index++) {
            if (value < 1024) {
                break;
            }
            value = value / 1024;
        }
        return String.format(Locale.getDefault(), "%.2f %s", value, fileSizeUnits[index]);
    }

    public static String calculateProperCount(long count) {
        if (count < 1000) {
            return String.valueOf(count);
        }
        double value = count;
        int index;
        for (index = 0; index < countUnits.length; index++) {
            if (value < 1000) {
                break;
            }
            value = value / 1000.0;
        }
        if (value < 10 && (value % 1.0) >= 0.049 && (value % 1.0) < 0.5) {
            return String.format(Locale.getDefault(), "%.1f %s", value, countUnits[index]);
        } else {
            return String.format(Locale.getDefault(), "%.0f %s", value, countUnits[index]);
        }
    }
}
