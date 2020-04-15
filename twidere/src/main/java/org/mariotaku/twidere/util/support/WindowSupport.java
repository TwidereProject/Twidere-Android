package org.mariotaku.twidere.util.support;

import android.annotation.TargetApi;
import android.os.Build;
import androidx.annotation.NonNull;
import android.view.View;
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

    public static void setNavigationBarColor(Window window, int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        WindowAccessorLollipop.setNavigationBarColor(window, color);
    }

    public static void setSharedElementsUseOverlay(Window window, boolean sharedElementsUseOverlay) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        WindowAccessorLollipop.setSharedElementsUseOverlay(window, sharedElementsUseOverlay);
    }

    public static void setLightStatusBar(@NonNull Window window, boolean lightStatusBar) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        final View decorView = window.getDecorView();

        final int systemUiVisibility = decorView.getSystemUiVisibility();
        if (lightStatusBar) {
            decorView.setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {
            decorView.setSystemUiVisibility(systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class WindowAccessorLollipop {
        public static void setStatusBarColor(Window window, int color) {
            window.setStatusBarColor(color);
        }

        public static void setNavigationBarColor(Window window, int color) {
            window.setNavigationBarColor(color);
        }

        public static void setSharedElementsUseOverlay(Window window, boolean sharedElementsUseOverlay) {
            window.setSharedElementsUseOverlay(sharedElementsUseOverlay);
        }
    }
}
