package org.mariotaku.chameleon;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.chameleon.internal.ChameleonInflationFactory;

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !statusBarColorHandled) {
            activity.getWindow().setStatusBarColor(theme.getStatusBarColor());
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
        private int colorPrimary;
        private int colorAccent;
        private int colorToolbar;
        private int colorBackground;
        private int colorForeground;
        private boolean toolbarColored;
        private int textColorPrimary;
        private int statusBarColor;

        Theme() {

        }

        public int getColorAccent() {
            return colorAccent;
        }

        public void setColorAccent(int colorAccent) {
            this.colorAccent = colorAccent;
        }

        public int getColorPrimary() {
            return colorPrimary;
        }

        public void setColorPrimary(int colorPrimary) {
            this.colorPrimary = colorPrimary;
        }

        public int getColorToolbar() {
            if (colorToolbar == 0) return colorPrimary;
            return colorToolbar;
        }

        public void setColorToolbar(int colorToolbar) {
            this.colorToolbar = colorToolbar;
        }

        public boolean isToolbarColored() {
            return toolbarColored;
        }

        public void setToolbarColored(boolean toolbarColored) {
            this.toolbarColored = toolbarColored;
        }

        public int getTextColorPrimary() {
            return textColorPrimary;
        }

        public void setTextColorPrimary(int textColorPrimary) {
            this.textColorPrimary = textColorPrimary;
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

        @NonNull
        public static Theme from(Context context) {
            Theme theme = new Theme();
            TypedArray a = context.obtainStyledAttributes(R.styleable.ChameleonTheme);
            theme.setColorPrimary(a.getColor(R.styleable.ChameleonTheme_colorPrimary, 0));
            theme.setColorAccent(a.getColor(R.styleable.ChameleonTheme_colorAccent, 0));
            theme.setColorToolbar(a.getColor(R.styleable.ChameleonTheme_colorToolbar, theme.getColorPrimary()));
            theme.setColorBackground(a.getColor(R.styleable.ChameleonTheme_android_colorBackground, 0));
            theme.setColorForeground(a.getColor(R.styleable.ChameleonTheme_android_colorForeground, 0));
            theme.setToolbarColored(a.getBoolean(R.styleable.ChameleonTheme_isToolbarColored, true));
            a.recycle();
            return theme;
        }

        public int getStatusBarColor() {
            if (statusBarColor == 0) {
                return ChameleonUtils.darkenColor(getColorToolbar());
            }
            return statusBarColor;
        }

        public void setStatusBarColor(int statusBarColor) {
            this.statusBarColor = statusBarColor;
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
