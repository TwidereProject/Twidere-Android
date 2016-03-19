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
import org.mariotaku.twidere.view.iface.IIconActionButton;

/**
 * Created by mariotaku on 14/11/5.
 */
public class IconActionButton extends AppCompatImageButton implements IIconActionButton {

    @ColorInt
    private int mDefaultColor, mActivatedColor, mDisabledColor;

    public IconActionButton(Context context) {
        this(context, null);
    }

    public IconActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.imageButtonStyle);
    }

    public IconActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconActionButton,
                R.attr.cardActionButtonStyle, R.style.Widget_CardActionButton);
        mDefaultColor = a.getColor(R.styleable.IconActionButton_iabColor, 0);
        mActivatedColor = a.getColor(R.styleable.IconActionButton_iabActivatedColor, 0);
        mDisabledColor = a.getColor(R.styleable.IconActionButton_iabDisabledColor, 0);
        a.recycle();
        updateColorFilter();
    }

    @Override
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

    @Override
    @ColorInt
    public int getActivatedColor() {
        if (mActivatedColor != 0) return mActivatedColor;
        return getDefaultColor();
    }

    @Override
    @ColorInt
    public int getDisabledColor() {
        if (mDisabledColor != 0) return mDisabledColor;
        return getDefaultColor();
    }

    @Override
    public void setDefaultColor(@ColorInt int defaultColor) {
        mDefaultColor = defaultColor;
        updateColorFilter();
    }

    @Override
    public void setActivatedColor(@ColorInt int activatedColor) {
        mActivatedColor = activatedColor;
        updateColorFilter();
    }

    @Override
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
