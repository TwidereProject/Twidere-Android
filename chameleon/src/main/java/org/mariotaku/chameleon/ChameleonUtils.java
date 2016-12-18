package org.mariotaku.chameleon;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.util.AttributeSet;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class ChameleonUtils {
    public static int findAttributeIndex(AttributeSet attributeSet, int attributeNameResource) {
        for (int i = 0, j = attributeSet.getAttributeCount(); i < j; i++) {
            if (attributeSet.getAttributeNameResource(i) == attributeNameResource) return i;
        }
        return -1;
    }

    public static boolean isColorLight(@ColorInt int color) {
        if (color == Color.BLACK) return false;
        else if (color == Color.WHITE || color == Color.TRANSPARENT) return true;
        final double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.4;
    }

    @ColorInt
    public static int shiftColor(@ColorInt int color, @FloatRange(from = 0.0f, to = 2.0f) float by) {
        if (by == 1f) return color;
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= by; // value component
        return Color.HSVToColor(hsv);
    }

    @ColorInt
    public static int darkenColor(@ColorInt int color) {
        return shiftColor(color, 0.9f);
    }

    public static Activity getActivity(Context context) {
        if (context instanceof Activity) return (Activity) context;
        if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }
}
