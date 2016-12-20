package org.mariotaku.chameleon.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.Window;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.Chameleon.Theme.LightStatusBarMode;
import org.mariotaku.chameleon.ChameleonUtils;
import org.mariotaku.chameleon.ChameleonView;
import org.mariotaku.chameleon.internal.SupportMethods;

/**
 * Created by mariotaku on 2016/12/19.
 */

public class ChameleonDrawerLayout extends DrawerLayout implements ChameleonView, ChameleonView.StatusBarThemeable {

    public ChameleonDrawerLayout(Context context) {
        super(context);
    }

    public ChameleonDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChameleonDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isPostApplyTheme() {
        return false;
    }

    @Nullable
    @Override
    public Appearance createAppearance(Context context, AttributeSet attributeSet, Chameleon.Theme theme) {
        Appearance appearance = new Appearance();
        appearance.setStatusBarColor(theme.getStatusBarColor());
        appearance.setLightStatusBarMode(theme.getLightStatusBarMode());
        return appearance;
    }

    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        Appearance a = (Appearance) appearance;
        final int statusBarColor = a.getStatusBarColor();
        final Activity activity = ChameleonUtils.getActivity(getContext());
        if (activity != null) {
            final Window window = activity.getWindow();
            SupportMethods.setStatusBarColor(window, Color.TRANSPARENT);
            ChameleonUtils.applyLightStatusBar(window, statusBarColor, a.getLightStatusBarMode());
        }
        setStatusBarBackgroundColor(statusBarColor);
    }

    @Override
    public boolean isStatusBarColorHandled() {
        return true;
    }

    public static class Appearance implements ChameleonView.Appearance {
        int statusBarColor;
        @LightStatusBarMode
        int lightStatusBarMode;

        public int getStatusBarColor() {
            return statusBarColor;
        }

        public void setStatusBarColor(int statusBarBackgroundColor) {
            this.statusBarColor = statusBarBackgroundColor;
        }

        @LightStatusBarMode
        public int getLightStatusBarMode() {
            return lightStatusBarMode;
        }

        public void setLightStatusBarMode(@LightStatusBarMode int lightStatusBarMode) {
            this.lightStatusBarMode = lightStatusBarMode;
        }
    }
}
