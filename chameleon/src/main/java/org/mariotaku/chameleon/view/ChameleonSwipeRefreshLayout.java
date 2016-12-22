package org.mariotaku.chameleon.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonView;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class ChameleonSwipeRefreshLayout extends SwipeRefreshLayout implements ChameleonView {

    public ChameleonSwipeRefreshLayout(Context context) {
        super(context);
    }

    public ChameleonSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean isPostApplyTheme() {
        return false;
    }

    @Nullable
    @Override
    public Appearance createAppearance(@NonNull Context context, @NonNull AttributeSet attributeSet, @NonNull Chameleon.Theme theme) {
        Appearance appearance = new Appearance();
        appearance.indicatorColor = theme.getColorAccent();
        appearance.progressBackgroundColor = theme.getColorBackground();
        return appearance;
    }


    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        final Appearance a = (Appearance) appearance;
        setColorSchemeColors(a.indicatorColor);
        setProgressBackgroundColorSchemeColor(a.progressBackgroundColor);
    }

    public static class Appearance implements ChameleonView.Appearance {
        int indicatorColor;
        int progressBackgroundColor;
    }
}
