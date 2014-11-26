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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by mariotaku on 14/11/26.
 */
public class HomeContentFrameLayout extends FrameLayout {

    private final Paint mPaint;

    private int mStatusBarHeight;

    public HomeContentFrameLayout(Context context) {
        this(context, null);
    }

    public HomeContentFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomeContentFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setWillNotDraw(false);
    }

    public void setColor(int color, int alpha) {
        mPaint.setColor(color);
        mPaint.setAlpha(alpha);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, canvas.getWidth(), mStatusBarHeight, mPaint);
    }

    public void setStatusBarHeight(int height) {
        mStatusBarHeight = height;
        setPadding(0, height, 0, 0);
        invalidate();
    }

}
