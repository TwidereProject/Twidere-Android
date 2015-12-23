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
        final CodePointArray array = new CodePointArray(text);
        for (int i = array.length() - 1; i >= 0; i--) {
            final int codePoint = array.get(i);
            if (isEmoji(codePoint)) {
                int arrayIdx = i, arrayEnd = i + 1;
                int textIdx = array.indexOfText(codePoint, i);
                if (textIdx == -1 || textIdx < textStart) {
                    continue;
                }
                final int textEnd = textIdx + Character.charCount(codePoint);
                if (isRegionalIndicatorSymbol(codePoint)) {
                    if (i > 0) {
                        int prev = array.get(i - 1);
                        if (isRegionalIndicatorSymbol(prev)) {
                            textIdx -= Character.charCount(prev);
                            arrayIdx--;
                            i--;
                        }
                    }
                } else if (isModifier(codePoint)) {
                    if (i > 0) {
                        int prev = array.get(i - 1);
                        if (isEmoji(prev)) {
                            textIdx -= Character.charCount(prev);
                            arrayIdx--;
                            i--;
                        }
                    }
                }
                if (textEnd > textStart + textLength) continue;
                final EmojiSpan[] spans = text.getSpans(textIdx, textEnd, EmojiSpan.class);
                if (spans.length > 0) continue;
                final Drawable drawable = emoji.getEmojiDrawableFor(array.subarray(arrayIdx, arrayEnd));
                if (drawable == null) continue;
                text.setSpan(new EmojiSpan(drawable), textIdx, textEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static boolean isModifier(int codePoint) {
        return inRange(codePoint, 0x1f3fb, 0x1f3ff);
    }

    private static boolean isEmoji(int codePoint) {
        return !Character.isLetterOrDigit(codePoint);
    }

    private static boolean inRange(int codePoint, int from, int to) {
        return codePoint >= from && codePoint <= to;
    }

    private static boolean isRegionalIndicatorSymbol(int codePoint) {
        return inRange(codePoint, 0x1f1e6, 0x1f1ff);
    }

}
