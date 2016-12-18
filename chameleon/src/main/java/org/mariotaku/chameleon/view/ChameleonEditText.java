package org.mariotaku.chameleon.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.widget.TextView;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonTypedArray;
import org.mariotaku.chameleon.ChameleonView;
import org.mariotaku.chameleon.R;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class ChameleonEditText extends AppCompatEditText implements ChameleonView {
    public ChameleonEditText(Context context) {
        super(context);
    }

    public ChameleonEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChameleonEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isPostApplyTheme() {
        return false;
    }

    @Nullable
    @Override
    public Appearance createAppearance(Context context, AttributeSet attributeSet, Chameleon.Theme theme) {
        return Appearance.create(context, attributeSet, theme);
    }


    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        final Appearance a = (Appearance) appearance;
        Appearance.apply(this, a);
    }

    public static class Appearance extends ChameleonTextView.Appearance {
        private int backgroundColor;

        public int getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public static void apply(TextView view, Appearance appearance) {
            view.setLinkTextColor(appearance.getLinkTextColor());
            ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(appearance.getBackgroundColor()));
        }

        public static Appearance create(Context context, AttributeSet attributeSet, Chameleon.Theme theme) {
            Appearance appearance = new Appearance();
            ChameleonTypedArray a = ChameleonTypedArray.obtain(context, attributeSet,
                    R.styleable.ChameleonEditText, theme);
            appearance.setLinkTextColor(a.getColor(R.styleable.ChameleonEditText_android_textColorLink, theme.getColorAccent()));
            appearance.setBackgroundColor(a.getColor(R.styleable.ChameleonEditText_backgroundTint, theme.getColorAccent()));
            a.recycle();
            return appearance;
        }
    }
}
