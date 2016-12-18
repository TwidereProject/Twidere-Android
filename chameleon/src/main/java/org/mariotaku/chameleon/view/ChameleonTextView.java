package org.mariotaku.chameleon.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonTypedArray;
import org.mariotaku.chameleon.ChameleonView;
import org.mariotaku.chameleon.R;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class ChameleonTextView extends AppCompatTextView implements ChameleonView {
    public ChameleonTextView(Context context) {
        super(context);
    }

    public ChameleonTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChameleonTextView(Context context, AttributeSet attrs, int defStyleAttr) {
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
                R.styleable.ChameleonTextView, theme);
        appearance.setLinkTextColor(a.getColor(R.styleable.ChameleonTextView_android_textColorLink, theme.getColorAccent()));
        a.recycle();
        return appearance;
    }


    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        final Appearance a = (Appearance) appearance;
        setLinkTextColor(a.getLinkTextColor());
    }

    public static class Appearance implements ChameleonView.Appearance {
        private int linkTextColor;

        public int getLinkTextColor() {
            return linkTextColor;
        }

        public void setLinkTextColor(int linkTextColor) {
            this.linkTextColor = linkTextColor;
        }
    }
}
