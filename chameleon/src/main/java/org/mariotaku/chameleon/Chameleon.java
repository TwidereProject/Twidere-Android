package org.mariotaku.chameleon;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import org.mariotaku.chameleon.internal.ChameleonInflationFactory;
import org.mariotaku.chameleon.internal.WindowSupport;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class Chameleon {

    private final Activity activity;
    private final Theme theme;
    private final ArrayMap<ChameleonView, ChameleonView.Appearance> postApplyViews;

    private Chameleon(Activity activity) {
        this.activity = activity;
        this.theme = getOverrideTheme(activity, activity);
        this.postApplyViews = new ArrayMap<>();
    }

    public static Chameleon getInstance(Activity activity) {
        return new Chameleon(activity);
    }

    public void preApply() {

        final LayoutInflater inflater = activity.getLayoutInflater();
        AppCompatDelegate delegate = null;
        if (activity instanceof AppCompatActivity) {
            delegate = ((AppCompatActivity) activity).getDelegate();
        }
        final ChameleonInflationFactory factory = new ChameleonInflationFactory(inflater, activity,
                delegate, theme, postApplyViews);
        LayoutInflaterCompat.setFactory(inflater, factory);
    }

    public void postApply() {
        for (int i = 0, j = postApplyViews.size(); i < j; i++) {
            postApplyViews.keyAt(i).applyAppearance(postApplyViews.valueAt(i));
        }
        postApplyViews.clear();

        boolean statusBarColorHandled = false;
        final View rootView = getRootView();
        if (rootView instanceof ChameleonView.StatusBarThemeable) {
            if (((ChameleonView.StatusBarThemeable) rootView).isStatusBarColorHandled()) {
                statusBarColorHandled = true;
            }
        }
        if (!statusBarColorHandled) {
            final Window window = activity.getWindow();
            final int statusBarColor = theme.getStatusBarColor();
            WindowSupport.setStatusBarColor(window, statusBarColor);
            ChameleonUtils.applyLightStatusBar(window, statusBarColor, theme.getLightStatusBarMode());
        }
    }

    private View getRootView() {
        return ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
    }

    public void invalidateActivity() {

    }

    public void cleanUp() {
        postApplyViews.clear();
    }

    public void themeOverflow() {

    }

    @NonNull
    public static Theme getOverrideTheme(Context context, Object obj) {
        if (obj instanceof Themeable) {
            final Theme theme = ((Themeable) obj).getOverrideTheme();
            if (theme != null) {
                return theme;
            }
        }
        return Theme.from(context);
    }

    /**
     * Created by mariotaku on 2016/12/18.
     */

    public static class Theme {
        private int colorBackground;
        private int colorForeground;

        private int textColorPrimary;
        private int textColorSecondary;
        private int textColorLink;

        private int colorPrimary;
        private int colorPrimaryDark;
        private int colorAccent;

        private int colorEdgeEffect;

        private int colorControlNormal;
        private int colorControlActivated;

        private int statusBarColor;
        private int navigationBarColor;

        private int colorToolbar;
        private boolean toolbarColored;
        @LightStatusBarMode
        private int lightStatusBarMode = LightStatusBarMode.NONE;

        Theme() {

        }

        public int getColorPrimary() {
            return colorPrimary;
        }

        public void setColorPrimary(int colorPrimary) {
            this.colorPrimary = colorPrimary;
        }

        public int getColorPrimaryDark() {
            if (colorPrimaryDark == 0) {
                return ChameleonUtils.darkenColor(getColorPrimary());
            }
            return colorPrimaryDark;
        }

        public void setColorPrimaryDark(int colorPrimaryDark) {
            this.colorPrimaryDark = colorPrimaryDark;
        }

        public int getColorAccent() {
            return colorAccent;
        }

        public void setColorAccent(int color) {
            this.colorAccent = color;
        }

        public int getColorEdgeEffect() {
            if (colorEdgeEffect == 0) {
                return getColorPrimary();
            }
            return colorEdgeEffect;
        }

        public void setColorEdgeEffect(int color) {
            if (color == colorPrimary) return;
            this.colorEdgeEffect = color;
        }

        public int getColorToolbar() {
            if (colorToolbar == 0) return colorPrimary;
            return colorToolbar;
        }

        public void setColorToolbar(int color) {
            this.colorToolbar = color;
        }

        public boolean isToolbarColored() {
            return toolbarColored;
        }

        public void setToolbarColored(boolean toolbarColored) {
            this.toolbarColored = toolbarColored;
        }

        public int getColorBackground() {
            return colorBackground;
        }

        public void setColorBackground(int colorBackground) {
            this.colorBackground = colorBackground;
        }

        public int getColorForeground() {
            return colorForeground;
        }

        public void setColorForeground(int colorForeground) {
            this.colorForeground = colorForeground;
        }

        public int getTextColorPrimary() {
            return textColorPrimary;
        }

        public void setTextColorPrimary(int textColorPrimary) {
            this.textColorPrimary = textColorPrimary;
        }

        public int getTextColorSecondary() {
            return textColorSecondary;
        }

        public void setTextColorSecondary(int textColorSecondary) {
            this.textColorSecondary = textColorSecondary;
        }

        public int getTextColorLink() {
            if (textColorLink == 0) {
                return getColorAccent();
            }
            return textColorLink;
        }

        public void setTextColorLink(int textColorLink) {
            this.textColorLink = textColorLink;
        }

        public int getStatusBarColor() {
            if (statusBarColor == 0) {
                return getColorPrimaryDark();
            }
            return statusBarColor;
        }

        public void setStatusBarColor(int statusBarColor) {
            this.statusBarColor = statusBarColor;
        }

        public int getNavigationBarColor() {
            return navigationBarColor;
        }

        public void setNavigationBarColor(int navigationBarColor) {
            this.navigationBarColor = navigationBarColor;
        }

        public void setColorControlNormal(int color) {
            if (color == textColorSecondary) return;
            this.colorControlNormal = color;
        }

        public int getColorControlNormal() {
            if (colorControlNormal == 0) {
                return getTextColorSecondary();
            }
            return colorControlNormal;
        }

        public int getColorControlActivated() {
            if (colorControlActivated == 0) {
                return getColorAccent();
            }
            return colorControlActivated;
        }

        public void setColorControlActivated(int color) {
            if (color == colorAccent) return;
            this.colorControlActivated = color;
        }

        public void setLightStatusBarMode(@LightStatusBarMode int mode) {
            this.lightStatusBarMode = mode;
        }

        @LightStatusBarMode
        public int getLightStatusBarMode() {
            return lightStatusBarMode;
        }

        @NonNull
        public static Theme from(Context context) {
            Theme theme = new Theme();
            TypedArray a = context.obtainStyledAttributes(R.styleable.ChameleonTheme);
            theme.setColorBackground(a.getColor(R.styleable.ChameleonTheme_android_colorBackground, 0));
            theme.setColorForeground(a.getColor(R.styleable.ChameleonTheme_android_colorForeground, 0));

            theme.setColorPrimary(a.getColor(R.styleable.ChameleonTheme_colorPrimary, 0));
            theme.setColorPrimaryDark(a.getColor(R.styleable.ChameleonTheme_colorPrimaryDark, 0));
            theme.setColorAccent(a.getColor(R.styleable.ChameleonTheme_colorAccent, 0));

            theme.setTextColorPrimary(a.getColor(R.styleable.ChameleonTheme_android_textColorPrimary, 0));
            theme.setTextColorSecondary(a.getColor(R.styleable.ChameleonTheme_android_textColorSecondary, 0));
            theme.setTextColorLink(a.getColor(R.styleable.ChameleonTheme_android_textColorLink, 0));

            theme.setColorEdgeEffect(a.getColor(R.styleable.ChameleonTheme_colorEdgeEffect, 0));

            theme.setColorControlNormal(a.getColor(R.styleable.ChameleonTheme_colorControlNormal, 0));
            theme.setColorControlActivated(a.getColor(R.styleable.ChameleonTheme_colorControlNormal, 0));

            theme.setStatusBarColor(a.getColor(R.styleable.ChameleonTheme_android_statusBarColor, 0));
            theme.setNavigationBarColor(a.getColor(R.styleable.ChameleonTheme_android_navigationBarColor, Color.BLACK));

            theme.setColorToolbar(a.getColor(R.styleable.ChameleonTheme_colorToolbar, theme.getColorPrimary()));
            theme.setToolbarColored(a.getBoolean(R.styleable.ChameleonTheme_isToolbarColored, true));

            a.recycle();
            return theme;
        }

        @IntDef({LightStatusBarMode.NONE, LightStatusBarMode.AUTO, LightStatusBarMode.ON,
                LightStatusBarMode.OFF})
        public @interface LightStatusBarMode {
            int NONE = 0;
            int AUTO = 1;
            int ON = 2;
            int OFF = 3;
        }
    }

    /**
     * Created by mariotaku on 2016/12/18.
     */

    public interface Themeable {
        @Nullable
        Theme getOverrideTheme();
    }
}
