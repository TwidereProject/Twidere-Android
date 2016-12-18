package org.mariotaku.chameleon.view;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonTypedArray;
import org.mariotaku.chameleon.ChameleonView;
import org.mariotaku.chameleon.R;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class ChameleonToolbar extends Toolbar implements ChameleonView {
    public ChameleonToolbar(Context context) {
        super(context);
    }

    public ChameleonToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ChameleonToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
                R.styleable.ChameleonToolbar, theme);
        final Drawable background = a.getDrawable(R.styleable.ChameleonToolbar_android_background);
        if (background != null) {
            appearance.setBackground(background);
        } else {
            appearance.setBackground(new ColorDrawable(theme.getColorToolbar()));
        }
        appearance.setTitleTextColor(a.getColor(R.styleable.ChameleonToolbar_titleTextColor));
        appearance.setSubTitleTextColor(a.getColor(R.styleable.ChameleonToolbar_subtitleTextColor));
        a.recycle();
        return appearance;
    }

    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        Appearance a = (Appearance) appearance;
        setBackgroundDrawable(a.getDrawable());
        setTitleTextColor(a.getTitleTextColor());
        setSubtitleTextColor(a.getSubTitleTextColor());
    }

    public static class Appearance implements ChameleonView.Appearance {

        private int titleTextColor;
        private int subTitleTextColor;
        private Drawable background;

        public void setTitleTextColor(int titleTextColor) {
            this.titleTextColor = titleTextColor;
        }

        public int getTitleTextColor() {
            return titleTextColor;
        }

        public void setSubTitleTextColor(int subTitleTextColor) {
            this.subTitleTextColor = subTitleTextColor;
        }

        public int getSubTitleTextColor() {
            return subTitleTextColor;
        }

        public void setBackground(Drawable background) {
            this.background = background;
        }

        public Drawable getDrawable() {
            return background;
        }
    }
}
