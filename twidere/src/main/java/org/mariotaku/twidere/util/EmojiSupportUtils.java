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

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.Spanned;

import org.mariotaku.twidere.text.style.EmojiSpan;

/**
 * Created by mariotaku on 15/12/20.
 */
public class EmojiSupportUtils {
    public static void applyEmoji(ExternalThemeManager manager, @NonNull Spannable text) {
        applyEmoji(manager, text, 0, text.length());
    }

    public static void applyEmoji(ExternalThemeManager manager, @NonNull Spannable text,
                                  int textStart, int textLength) {
        final ExternalThemeManager.Emoji emoji = manager.getEmoji();
        if (!emoji.isSupported()) return;
        CodePointArray array = new CodePointArray(text);
        for (int i = 0, j = array.length(); i < j; i++) {
            final int codePoint = array.get(i);
            if (isEmoji(codePoint)) {
                final int idx = array.indexOfText(codePoint, i);
                if (idx == -1 || idx < textStart) {
                    continue;
                }
                final int end = idx + Character.charCount(codePoint);
                if (end > textStart + textLength) continue;
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

}
