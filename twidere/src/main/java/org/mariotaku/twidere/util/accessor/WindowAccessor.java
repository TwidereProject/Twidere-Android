package org.mariotaku.twidere.util.accessor;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.Window;

/**
 * Created by mariotaku on 14/10/23.
 */
public class WindowAccessor {
    public static void setStatusBarColor(Window window, int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        WindowAccessorL.setStatusBarColor(window, color);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class WindowAccessorL {
        public static void setStatusBarColor(Window window, int color) {
            window.setStatusBarColor(color);
        }
    }
}
