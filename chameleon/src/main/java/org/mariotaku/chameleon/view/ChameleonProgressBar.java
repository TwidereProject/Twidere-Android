package org.mariotaku.chameleon.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonView;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class ChameleonProgressBar extends ProgressBar implements ChameleonView {
    public ChameleonProgressBar(Context context) {
        super(context);
    }

    public ChameleonProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChameleonProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
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
        appearance.setProgressColor(theme.getColorAccent());
        return appearance;
    }


    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        final Appearance a = (Appearance) appearance;
        final Drawable indeterminateDrawable = getIndeterminateDrawable();
        if (indeterminateDrawable != null) {
            DrawableCompat.setTint(indeterminateDrawable, a.getProgressColor());
        }
        final Drawable progressDrawable = getProgressDrawable();
        if (progressDrawable != null) {
            DrawableCompat.setTint(progressDrawable, a.getProgressColor());
        }
    }

    public static class Appearance implements ChameleonView.Appearance {
        private int progressColor;

        public int getProgressColor() {
            return progressColor;
        }

        public void setProgressColor(int progressColor) {
            this.progressColor = progressColor;
        }
    }
}
