package org.mariotaku.chameleon.internal;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.Window;

/**
 * Created by mariotaku on 14/10/23.
 */
public class SupportMethods {
    private SupportMethods() {
    }

    public static void setStatusBarColor(Window window, int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        SupportLollipop.setStatusBarColor(window, color);
    }

    public static void setBackground(final View view, final Drawable background) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            //noinspection deprecation
            view.setBackgroundDrawable(background);
        } else {
            SupportJellyBean.setBackground(view, background);
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class SupportLollipop {
        public static void setStatusBarColor(Window window, int color) {
            window.setStatusBarColor(color);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    static class SupportJellyBean {
        private SupportJellyBean() {
        }

        static void setBackground(final View view, final Drawable background) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
            view.setBackground(background);
        }
    }
}
