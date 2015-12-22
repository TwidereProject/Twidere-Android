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

package org.mariotaku.twidere.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

/**
 * Created by mariotaku on 15/12/22.
 */
public class EmojiSpan extends DynamicDrawableSpan {
    private final Drawable drawable;

    public EmojiSpan(Drawable drawable) {
        super(ALIGN_BOTTOM);
        this.drawable = drawable;
    }

    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
        final Drawable drawable = getDrawable();
        if (drawable == null) return 0;
        final int textHeightPx = Math.round(paint.descent() - paint.ascent());
        final float intrinsicWidth = drawable.getIntrinsicWidth(),
                intrinsicHeight = drawable.getIntrinsicHeight();
        final int scaledWidth;
        if (intrinsicWidth > intrinsicHeight) {
            scaledWidth = Math.round(textHeightPx * (intrinsicWidth / intrinsicHeight));
        } else {
            scaledWidth = Math.round(intrinsicWidth * (textHeightPx / intrinsicHeight));
        }
        if (fm == null) {
            fm = paint.getFontMetricsInt();
        }
        final int top = fm.bottom - textHeightPx, left = 0;
        drawable.setBounds(left, top, left + scaledWidth, top + textHeightPx);
        return scaledWidth;
    }


    @Override
    public void draw(Canvas canvas, CharSequence text, int start,
                     int end, float x, int top, int y, int bottom,
                     Paint paint) {
        final Drawable b = getDrawable();
        if (b == null) return;
        canvas.save();
        canvas.translate(x, y);
        b.draw(canvas);
        canvas.restore();
    }

}
