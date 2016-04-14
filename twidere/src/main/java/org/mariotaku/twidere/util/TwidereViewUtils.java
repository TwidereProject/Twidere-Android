package org.mariotaku.twidere.util;

import android.support.annotation.UiThread;
import android.view.View;

/**
 * Created by mariotaku on 16/1/23.
 */
public class TwidereViewUtils {
    private TwidereViewUtils() {
    }

    @UiThread
    public static boolean hitView(float x, float y, View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return TwidereMathUtils.inRange(x, location[0], location[0] + view.getWidth(), TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE) &&
                TwidereMathUtils.inRange(y, location[1], location[1] + view.getHeight(), TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE);
    }
}
