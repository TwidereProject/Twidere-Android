package org.mariotaku.chameleon;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.graphics.drawable.DrawableWrapper;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarAccessor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DecorToolbar;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import org.mariotaku.chameleon.internal.ChameleonInflationFactory;
import org.mariotaku.chameleon.internal.SupportMethods;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class Chameleon {

    private final Activity activity;
    private final Theme theme;
    @Nullable
    private final AppearanceCreator creator;
    private final ArrayMap<ChameleonView, ChameleonView.Appearance> postApplyViews;

    private Chameleon(Activity activity, @Nullable AppearanceCreator creator) {
        this.activity = activity;
        this.creator = creator;
        this.theme = getOverrideTheme(activity, activity);
        this.postApplyViews = new ArrayMap<>();
    }

    public static Chameleon getInstance(Activity activity) {
        return getInstance(activity, null);
    }

    public static Chameleon getInstance(Activity activity, AppearanceCreator creator) {
        return new Chameleon(activity, creator);
    }

    @SuppressWarnings("WeakerAccess")
    public void preApply() {
        final LayoutInflater inflater = activity.getLayoutInflater();
        AppCompatDelegate delegate = null;
        if (activity instanceof AppCompatActivity) {
            delegate = ((AppCompatActivity) activity).getDelegate();
        }

        final ChameleonInflationFactory factory = new ChameleonInflationFactory(inflater, activity,
                creator, delegate, theme, postApplyViews);
        LayoutInflaterCompat.setFactory(inflater, factory);
    }

    @SuppressWarnings("WeakerAccess")
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
            SupportMethods.setStatusBarColor(window, statusBarColor);
            ChameleonUtils.applyLightStatusBar(window, statusBarColor, theme.getLightStatusBarMode());
        }
    }

    private View getRootView() {
        return ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
    }

    @SuppressWarnings("WeakerAccess")
    public void invalidateActivity() {

    }

    @SuppressWarnings("WeakerAccess")
    public void cleanUp() {
        postApplyViews.clear();
    }

    @SuppressWarnings("WeakerAccess")
    public void themeOverflow() {
        if (activity instanceof AppCompatActivity) {
            final ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            final DecorToolbar decorToolbar = ActionBarAccessor.getDecorToolbar(actionBar);
            if (decorToolbar != null) {
                Toolbar toolbar = (Toolbar) decorToolbar.getViewGroup();
                int itemColor = ChameleonUtils.getColorDependent(theme.getColorToolbar());
                ChameleonUtils.setOverflowIconColor(toolbar, itemColor);
            }
        }
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

    @SuppressWarnings("WeakerAccess")
    public void themeActionMenu(Menu menu) {
        int itemColor = ChameleonUtils.getColorDependent(theme.getColorToolbar());
        themeMenu(menu, itemColor);
    }

    private void themeMenu(Menu menu, int color) {
        for (int i = 0, j = menu.size(); i < j; i++) {
            themeMenuItem(menu.getItem(i), color);
        }
    }

    private void themeMenuItem(MenuItem item, int color) {
        if (item.hasSubMenu()) {
            themeMenu(item.getSubMenu(), color);
        }
        Drawable icon = item.getIcon();
        if (icon == null) return;
        if (!(icon instanceof DrawableWrapper)) {
            icon = DrawableCompat.wrap(icon);
            item.setIcon(icon);
        }
        DrawableCompat.setTint(icon, color);
    }

    public interface AppearanceCreator {
        @Nullable
        ChameleonView.Appearance createAppearance(@NonNull View view,
                                                  @NonNull Context context,
                                                  @NonNull AttributeSet attributeSet,
                                                  @NonNull Chameleon.Theme theme);

        void applyAppearance(@NonNull View view, @NonNull ChameleonView.Appearance appearance);
    }

    /**
     * Created by mariotaku on 2016/12/18.
     */
    public static class Theme {
        private int colorBackground;
        private int colorForeground;

        private int textColorPrimary;
        private int textColorPrimaryInverse;
        private int textColorSecondary;
        private int textColorSecondaryInverse;
        private int textColorLink;
        private int textColorLinkInverse;

        private int colorPrimary;
        private int colorPrimaryDark;
        private int colorAccent;

        private int colorEdgeEffect;

        private int colorControlNormal;
        private int colorControlActivated;
        private int colorControlHighlight;

        private int statusBarColor;
        private int navigationBarColor;

        private int colorToolbar;
        private boolean toolbarColored;
        @LightStatusBarMode
        private int lightStatusBarMode = LightStatusBarMode.NONE;

        private int actionBarWidgetTheme;

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

        public int getTextColorPrimaryInverse() {
            return textColorPrimaryInverse;
        }

        public void setTextColorPrimaryInverse(int textColorPrimaryInverse) {
            this.textColorPrimaryInverse = textColorPrimaryInverse;
        }

        public int getTextColorSecondaryInverse() {
            return textColorSecondaryInverse;
        }

        public void setTextColorSecondaryInverse(int textColorSecondaryInverse) {
            this.textColorSecondaryInverse = textColorSecondaryInverse;
        }

        public void setTextColorLink(int textColorLink) {
            this.textColorLink = textColorLink;
        }

        public int getTextColorLinkInverse() {
            return textColorLinkInverse;
        }

        public void setTextColorLinkInverse(int textColorLinkInverse) {
            this.textColorLinkInverse = textColorLinkInverse;
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

        public int getColorControlHighlight() {
            return colorControlHighlight;
        }

        public void setColorControlHighlight(int colorControlHighlight) {
            this.colorControlHighlight = colorControlHighlight;
        }

        public void setLightStatusBarMode(@LightStatusBarMode int mode) {
            this.lightStatusBarMode = mode;
        }

        @LightStatusBarMode
        public int getLightStatusBarMode() {
            return lightStatusBarMode;
        }

        public int getActionBarWidgetTheme() {
            return actionBarWidgetTheme;
        }

        public void setActionBarWidgetTheme(int actionBarWidgetTheme) {
            this.actionBarWidgetTheme = actionBarWidgetTheme;
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
            theme.setTextColorPrimaryInverse(a.getColor(R.styleable.ChameleonTheme_android_textColorPrimaryInverse, 0));
            theme.setTextColorSecondary(a.getColor(R.styleable.ChameleonTheme_android_textColorSecondary, 0));
            theme.setTextColorSecondaryInverse(a.getColor(R.styleable.ChameleonTheme_android_textColorSecondaryInverse, 0));
            theme.setTextColorLink(a.getColor(R.styleable.ChameleonTheme_android_textColorLink, 0));
            theme.setTextColorLinkInverse(a.getColor(R.styleable.ChameleonTheme_android_textColorLinkInverse, 0));

            theme.setColorEdgeEffect(a.getColor(R.styleable.ChameleonTheme_colorEdgeEffect, 0));

            theme.setColorControlNormal(a.getColor(R.styleable.ChameleonTheme_colorControlNormal, 0));
            theme.setColorControlActivated(a.getColor(R.styleable.ChameleonTheme_colorControlActivated, 0));
            theme.setColorControlHighlight(a.getColor(R.styleable.ChameleonTheme_colorControlHighlight, 0));

            theme.setStatusBarColor(a.getColor(R.styleable.ChameleonTheme_android_statusBarColor, 0));
            theme.setNavigationBarColor(a.getColor(R.styleable.ChameleonTheme_android_navigationBarColor, Color.BLACK));

            theme.setColorToolbar(a.getColor(R.styleable.ChameleonTheme_colorToolbar, theme.getColorPrimary()));
            theme.setToolbarColored(a.getBoolean(R.styleable.ChameleonTheme_isToolbarColored, true));
            theme.setActionBarWidgetTheme(a.getResourceId(R.styleable.ChameleonTheme_actionBarWidgetTheme, 0));

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
