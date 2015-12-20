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

package org.mariotaku.twidere.util;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ImageSpan;

/**
 * Created by mariotaku on 15/12/20.
 */
public class EmojiSupportUtils {
    public static void applyEmoji(ExternalThemeManager manager, Spannable text) {
        final ExternalThemeManager.Emoji emoji = manager.getEmoji();
        if (!emoji.isSupported()) return;
        CodePointArray array = new CodePointArray(text);
        for (int i = 0, j = array.length(); i < j; i++) {
            final int codePoint = array.get(i);
            if (isEmoji(codePoint)) {
                final int idx = array.indexOfText(codePoint, i);
                if (idx == -1) {
                    continue;
                }
                final int end = idx + Character.charCount(codePoint);
                final ExternalThemeManager.Emoji[] spans = text.getSpans(idx, end,
                        ExternalThemeManager.Emoji.class);
                if (spans.length > 0) continue;
                final Drawable drawable = emoji.getEmojiDrawableFor(codePoint);
                if (drawable == null) continue;
                text.setSpan(new EmojiSpan(drawable), idx, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static boolean isEmoji(int codePoint) {
        return inRange(codePoint, 0x1f300, 0x1f5ff) || inRange(codePoint, 0x2500, 0x2BEF)
                || inRange(codePoint, 0x1f600, 0x1f64f) || inRange(codePoint, 0x2702, 0x27b0);
    }

    private static boolean inRange(int codePoint, int from, int to) {
        return codePoint >= from && codePoint <= to;
    }

    private static class EmojiSpan extends ImageSpan {
        public EmojiSpan(Drawable drawable) {
            super(drawable);
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            final int textSizePx = Math.round(paint.getTextSize());
            final Drawable drawable = getDrawable();
            if (drawable != null) {
                drawable.setBounds(0, 0, textSizePx, textSizePx);
            }
            return textSizePx + textSizePx / 16;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start,
                         int end, float x, int top, int y, int bottom,
                         Paint paint) {
            Drawable b = getDrawable();
            if (b == null) return;
            canvas.save();

            int transY = bottom - b.getBounds().bottom;
            // this is the key
            transY -= paint.getFontMetricsInt().descent / 2;

            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        }

    }
}
