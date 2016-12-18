package org.mariotaku.chameleon;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;

import org.mariotaku.chameleon.internal.ChameleonInflationFactory;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class Chameleon {

    private final Activity activity;

    private Chameleon(Activity activity) {
        this.activity = activity;
    }

    public static Chameleon getInstance(Activity activity) {
        return new Chameleon(activity);
    }

    public void preApply() {
        final LayoutInflater inflater = activity.getLayoutInflater();
        final ChameleonInflationFactory factory = new ChameleonInflationFactory();
        LayoutInflaterCompat.setFactory(inflater, factory);
    }

    public void postApply() {

    }

    public void invalidateActivity() {

    }

    public void cleanUp() {

    }

    public void themeOverflow() {

    }

    @NonNull
    public static Theme getOverrideTheme(Context context, Object obj) {
        if (obj instanceof Themeable) {
            return ((Themeable) obj).getOverrideTheme();
        }
        return Theme.from(context);
    }

    /**
     * Created by mariotaku on 2016/12/18.
     */

    public static class Theme {
        int primaryColor;
        int accentColor;
        int toolbarColor;
        boolean toolbarColored;
        int textColorPrimary;

        public int getAccentColor() {
            return accentColor;
        }

        public void setAccentColor(int accentColor) {
            this.accentColor = accentColor;
        }

        public int getPrimaryColor() {
            return primaryColor;
        }

        public void setPrimaryColor(int primaryColor) {
            this.primaryColor = primaryColor;
        }

        public int getToolbarColor() {
            return toolbarColor;
        }

        public void setToolbarColor(int toolbarColor) {
            this.toolbarColor = toolbarColor;
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

        @NonNull
        public static Theme from(Context context) {
            Theme theme = new Theme();
            return theme;
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
