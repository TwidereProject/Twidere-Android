/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.themed.ThemedTextView;

/**
 * Created by mariotaku on 14/11/20.
 */
public class ActionIconThemedTextView extends ThemedTextView {

    private int mColor, mDisabledColor, mActivatedColor;

    public ActionIconThemedTextView(Context context) {
        this(context, null);
    }

    public ActionIconThemedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionIconThemedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconActionButton);
        mColor = a.getColor(R.styleable.IconActionButton_iabColor, 0);
        mDisabledColor = a.getColor(R.styleable.IconActionButton_iabDisabledColor, 0);
        mActivatedColor = a.getColor(R.styleable.IconActionButton_iabActivatedColor, 0);
        a.recycle();
    }

    public int getActivatedColor() {
        if (mActivatedColor != 0) return mActivatedColor;
        final ColorStateList colors = getLinkTextColors();
        if (colors != null) return colors.getDefaultColor();
        return getCurrentTextColor();
    }

    public int getColor() {
        if (mColor != 0) return mColor;
        final ColorStateList colors = getTextColors();
        if (colors != null) return colors.getDefaultColor();
        return getCurrentTextColor();
    }

    public int getDisabledColor() {
        if (mDisabledColor != 0) return mDisabledColor;
        final ColorStateList colors = getTextColors();
        if (colors != null) return colors.getColorForState(new int[0], colors.getDefaultColor());
        return getCurrentTextColor();
    }

    @Override
    public void setActivated(boolean activated) {
        super.setActivated(activated);
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void setCompoundDrawablesRelative(Drawable start, Drawable top, Drawable end, Drawable bottom) {
        super.setCompoundDrawablesRelative(start, top, end, bottom);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        for (Drawable d : getCompoundDrawables()) {
            if (d != null) {
                d.mutate();
                final int color;
                if (isActivated()) {
                    color = getActivatedColor();
                } else if (isEnabled()) {
                    color = getColor();
                } else {
                    color = getDisabledColor();
                }
                d.setColorFilter(color, Mode.SRC_ATOP);
            }
        }
    }

}
