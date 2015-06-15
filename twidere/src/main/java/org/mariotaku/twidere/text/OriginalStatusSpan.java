/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.text;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.graphics.ColorUtils;
import android.text.TextPaint;
import android.text.style.ReplacementSpan;

import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereColorUtils;

/**
 * Created by mariotaku on 15/6/15.
 */
public class OriginalStatusSpan extends ReplacementSpan {

    private final RectF mBounds;
    private final Paint mPaint;
    private final int[] mDarkLightColors;
    private final int mPadding;
    private float mCornerRadius;

    public OriginalStatusSpan(Context context) {
        mBounds = new RectF();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDarkLightColors = new int[2];
        final float density = context.getResources().getDisplayMetrics().density;
        mCornerRadius = density * 2;
        mPaint.setStrokeWidth(density);
        mPadding = (int) (density * 4);
        ThemeUtils.getDarkLightForegroundColors(context, mDarkLightColors);
    }

    @Override
    public int getSize(final Paint paint, final CharSequence text, final int start, final int end, final Paint.FontMetricsInt fm) {
        paint.setTextSize(paint.getTextSize() * 0.8f);
        return (int) paint.measureText(text, start, end) + mPadding * 2;
    }

    @Override
    public void draw(final Canvas canvas, final CharSequence text, final int start, final int end, final float x, final int top, final int y, final int bottom, final Paint paint) {
        if (!(paint instanceof TextPaint)) return;
        final TextPaint tp = (TextPaint) paint;
        mBounds.left = x;
        mBounds.right = x + paint.measureText(text, start, end) + mPadding * 2;
        mBounds.top = top;
        mBounds.bottom = bottom;
        final int innerTextColor = TwidereColorUtils.getContrastYIQ(tp.linkColor,
                ThemeUtils.ACCENT_COLOR_THRESHOLD, mDarkLightColors[0], mDarkLightColors[1]);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(tp.linkColor);
        mBounds.inset(mPaint.getStrokeWidth() / 2, mPaint.getStrokeWidth() / 2);
        canvas.drawRoundRect(mBounds, mCornerRadius, mCornerRadius, mPaint);
        mBounds.inset(-mPaint.getStrokeWidth() / 2, -mPaint.getStrokeWidth() / 2);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(ColorUtils.compositeColors(ColorUtils.setAlphaComponent(innerTextColor, 0x80), tp.linkColor));
        mBounds.inset(mPaint.getStrokeWidth() / 2, mPaint.getStrokeWidth() / 2);
        canvas.drawRoundRect(mBounds, mCornerRadius, mCornerRadius, mPaint);
        paint.setColor(innerTextColor);
        canvas.drawText(text, start, end, x + mPadding, top + (bottom - top) / 2 - (paint.descent() + paint.ascent()) / 2, paint);
    }
}
