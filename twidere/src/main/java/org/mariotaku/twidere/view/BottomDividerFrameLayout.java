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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by mariotaku on 14/12/4.
 */
public class BottomDividerFrameLayout extends FrameLayout {

    private final Drawable mDividerDrawable;

    public BottomDividerFrameLayout(Context context) {
        this(context, null);
    }

    public BottomDividerFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomDividerFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.divider});
        mDividerDrawable = a.getDrawable(0);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final Drawable divider = mDividerDrawable;
        if (divider != null) {
            divider.draw(canvas);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final Drawable divider = mDividerDrawable;
        if (divider != null) {
            final int drawableLeft = 0, drawableRight = drawableLeft + getMeasuredWidth();
            final int drawableBottom = getMeasuredHeight(), drawableTop = drawableBottom - divider.getIntrinsicHeight();
            divider.setBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
        }
    }
}
