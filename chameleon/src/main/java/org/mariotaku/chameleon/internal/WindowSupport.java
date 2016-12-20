package org.mariotaku.chameleon.internal;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.Window;

/**
 * Created by mariotaku on 14/10/23.
 */
public class WindowSupport {
    private WindowSupport() {
    }

    public static void setStatusBarColor(Window window, int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        WindowAccessorLollipop.setStatusBarColor(window, color);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class WindowAccessorLollipop {
        public static void setStatusBarColor(Window window, int color) {
            window.setStatusBarColor(color);
        }

    }
}
