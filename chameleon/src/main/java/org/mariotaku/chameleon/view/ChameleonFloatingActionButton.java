package org.mariotaku.chameleon.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonTypedArray;
import org.mariotaku.chameleon.ChameleonView;
import org.mariotaku.chameleon.R;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class ChameleonFloatingActionButton extends FloatingActionButton implements ChameleonView {
    public ChameleonFloatingActionButton(Context context) {
        super(context);
    }

    public ChameleonFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChameleonFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isPostApplyTheme() {
        return false;
    }

    @Nullable
    @Override
    public Appearance createAppearance(Context context, AttributeSet attributeSet, Chameleon.Theme theme) {
        Appearance appearance = new Appearance();
        ChameleonTypedArray a = ChameleonTypedArray.obtain(context, attributeSet,
                R.styleable.ChameleonFloatingActionButton, theme);
        appearance.backgroundTint = a.getColor(R.styleable.ChameleonFloatingActionButton_backgroundTint, theme.getColorAccent());
        a.recycle();
        return appearance;
    }

    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        Appearance a = (Appearance) appearance;
        setBackgroundTintList(ColorStateList.valueOf(a.backgroundTint));
    }

    public static class Appearance implements ChameleonView.Appearance {
        int backgroundTint;
    }

}
