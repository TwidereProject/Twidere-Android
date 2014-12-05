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
import android.widget.TextView;

import org.mariotaku.twidere.R;

/**
 * Created by mariotaku on 14/11/20.
 */
public class ActionIconTextView extends TextView {

    private int mColor;
    private int mActivatedColor;

    public ActionIconTextView(Context context) {
        this(context, null);
    }

    public ActionIconTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionIconTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray defaultValues = context.obtainStyledAttributes(
                new int[]{android.R.attr.colorActivatedHighlight});
        final int defaultActivatedColor = defaultValues.getColor(0, 0);
        defaultValues.recycle();
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconActionButton);
        mColor = a.getColor(R.styleable.IconActionButton_color, 0);
        mActivatedColor = a.getColor(R.styleable.IconActionButton_activatedColor, defaultActivatedColor);
        a.recycle();
        updateColorFilter();
    }

    public int getActivatedColor() {
        return mActivatedColor;
    }

    public int getColor() {
        if (mColor != 0) return mColor;
        final ColorStateList colors = getTextColors();
        if (colors != null) return colors.getDefaultColor();
        return getCurrentTextColor();
    }

    @Override
    public void setActivated(boolean activated) {
        super.setActivated(activated);
        updateColorFilter();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void setCompoundDrawablesRelative(Drawable start, Drawable top, Drawable end, Drawable bottom) {
        super.setCompoundDrawablesRelative(start, top, end, bottom);
        updateColorFilter();
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
        updateColorFilter();
    }

    private void updateColorFilter() {
        for (Drawable d : getCompoundDrawables()) {
            if (d != null) {
                d.mutate();
                d.setColorFilter(isActivated() ? getActivatedColor() : getColor(), Mode.SRC_ATOP);
            }
        }

    }
}
