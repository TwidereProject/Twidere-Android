package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ThemeUtils;

/**
 * Created by mariotaku on 14/11/5.
 */
public class ActionIconButton extends AppCompatImageButton {

    @ColorInt
    private int mDefaultColor, mActivatedColor, mDisabledColor;

    public ActionIconButton(Context context) {
        this(context, null);
    }

    public ActionIconButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.imageButtonStyle);
    }

    public ActionIconButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconActionButton,
                R.attr.cardActionButtonStyle, R.style.Widget_CardActionButton);
        mDefaultColor = a.getColor(R.styleable.IconActionButton_iabColor, 0);
        mActivatedColor = a.getColor(R.styleable.IconActionButton_iabActivatedColor, 0);
        mDisabledColor = a.getColor(R.styleable.IconActionButton_iabDisabledColor, 0);
        a.recycle();
        updateColorFilter();
    }

    @ColorInt
    public int getDefaultColor() {
        if (mDefaultColor == 0) {
            // Return inverse color for background tint
            ColorStateList color = ViewCompat.getBackgroundTintList(this);
            if (color != null) {
                final int currentColor = color.getColorForState(getDrawableState(), 0);
                return ThemeUtils.getContrastColor(currentColor, Color.BLACK, Color.WHITE);
            }
        }
        return mDefaultColor;
    }

    @ColorInt
    public int getActivatedColor() {
        if (mActivatedColor != 0) return mActivatedColor;
        return getDefaultColor();
    }

    @ColorInt
    public int getDisabledColor() {
        if (mDisabledColor != 0) return mDisabledColor;
        return getDefaultColor();
    }

    public void setDefaultColor(@ColorInt int defaultColor) {
        mDefaultColor = defaultColor;
        updateColorFilter();
    }

    public void setActivatedColor(@ColorInt int activatedColor) {
        mActivatedColor = activatedColor;
        updateColorFilter();
    }

    public void setDisabledColor(@ColorInt int disabledColor) {
        mDisabledColor = disabledColor;
        updateColorFilter();
    }

    @Override
    public void setActivated(boolean activated) {
        super.setActivated(activated);
        updateColorFilter();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateColorFilter();
    }

    private void updateColorFilter() {
        if (isActivated()) {
            setColorFilter(getActivatedColor());
        } else if (isEnabled()) {
            setColorFilter(getDefaultColor());
        } else {
            setColorFilter(getDisabledColor());
        }
    }
}
