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
import android.widget.TextView;

import org.mariotaku.twidere.text.style.EmojiSpan;
import org.mariotaku.twidere.text.util.EmojiEditableFactory;
import org.mariotaku.twidere.text.util.EmojiSpannableFactory;

/**
 * Created by mariotaku on 15/12/20.
 */
public class EmojiSupportUtils {

    private EmojiSupportUtils() {
    }

    public static void initForTextView(TextView textView) {
        if (textView.isInEditMode()) return;
        textView.setSpannableFactory(new EmojiSpannableFactory(textView));
        textView.setEditableFactory(new EmojiEditableFactory(textView));
    }

    public static void applyEmoji(ExternalThemeManager manager, @NonNull Spannable text) {
        applyEmoji(manager, text, 0, text.length());
    }

    public static void applyEmoji(ExternalThemeManager manager, @NonNull Spannable text,
                                  int textStart, int textLength) {
        final ExternalThemeManager.Emoji emoji = manager.getEmoji();
        if (emoji == null || !emoji.isSupported()) return;
        final CodePointArray array = new CodePointArray(text);
        for (int arrayIdx = array.length() - 1; arrayIdx >= 0; arrayIdx--) {
            final int codePoint = array.get(arrayIdx);
            if (isEmoji(codePoint)) {
                int arrayEnd = arrayIdx + 1, arrayIdxOffset = 0;
                int textIdx = array.indexOfText(codePoint, arrayIdx), textIdxOffset = 0;
                int skippedIndex = 0;
                if (textIdx == -1 || textIdx < textStart) {
                    continue;
                }
                final int textEnd = textIdx + Character.charCount(codePoint);
                if (arrayIdx > 0) {
                    final int prevCodePoint = array.get(arrayIdx - 1);
                    if (isRegionalIndicatorSymbol(codePoint)) {
                        if (isRegionalIndicatorSymbol(prevCodePoint)) {
                            arrayIdxOffset = -1;
                            textIdxOffset = -Character.charCount(prevCodePoint);
                            skippedIndex = -1;
                        }
                    } else if (isModifier(codePoint)) {
                        if (isEmoji(prevCodePoint)) {
                            arrayIdxOffset = -1;
                            textIdxOffset = -Character.charCount(prevCodePoint);
                            skippedIndex = -1;
                        }
                    } else if (isKeyCap(codePoint)) {
                        if (isPhoneNumberSymbol(prevCodePoint)) {
                            arrayIdxOffset = -1;
                            textIdxOffset = -Character.charCount(prevCodePoint);
                            skippedIndex = -1;
                        }
                    } else if (isZeroWidthJoin(prevCodePoint)) {
                        int notValidControlCount = 0;
                        int charCount = 0;
                        for (int i = arrayIdx - 1; i >= 0; i--) {
                            final int cp = array.get(i);
                            charCount += Character.charCount(cp);
                            if (isZeroWidthJoin(cp) || isVariationSelector(cp)) {
                                // Ignore
                                notValidControlCount = 0;
                                continue;
                            }
                            notValidControlCount++;
                            if (notValidControlCount > 1 || i == 0) {
                                arrayIdxOffset = i - arrayIdx + 1;
                                textIdxOffset = -charCount + Character.charCount(cp);
                                skippedIndex = i - arrayIdx + 1;
                                break;
                            }
                        }
                    }
                }
                if (textEnd > textStart + textLength) continue;
                EmojiSpan[] spans = text.getSpans(textIdx + textIdxOffset, textEnd, EmojiSpan.class);
                if (spans.length == 0) {
                    Drawable drawable = emoji.getEmojiDrawableFor(array.subarray(arrayIdx + arrayIdxOffset,
                            arrayEnd));
                    if (drawable == null) {
                        // Not emoji combination, just use fallback
                        textIdxOffset = 0;
                        arrayIdxOffset = 0;
                        skippedIndex = 0;
                        spans = text.getSpans(textIdx + textIdxOffset, textEnd, EmojiSpan.class);
                        if (spans.length == 0) {
                            drawable = emoji.getEmojiDrawableFor(array.subarray(arrayIdx + arrayIdxOffset,
                                    arrayEnd));
                        }
                    }
                    if (drawable != null) {
                        text.setSpan(new EmojiSpan(drawable), textIdx + textIdxOffset, textEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
                arrayIdx += skippedIndex;
            }
        }
    }

    private static boolean isVariationSelector(int codePoint) {
        return codePoint == 0xfe0f;
    }

    private static boolean isZeroWidthJoin(int codePoint) {
        return codePoint == 0x200d;
    }

    private static boolean isPhoneNumberSymbol(int codePoint) {
        return codePoint == 0x0023 || codePoint == 0x002a || TwidereMathUtils.inRange(codePoint,
                0x0030, 0x0039, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE);
    }

    private static boolean isModifier(int codePoint) {
        return TwidereMathUtils.inRange(codePoint, 0x1f3fb, 0x1f3ff,
                TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE);
    }

    private static boolean isEmoji(int codePoint) {
        return !Character.isLetterOrDigit(codePoint);
    }

    private static boolean isRegionalIndicatorSymbol(int codePoint) {
        return TwidereMathUtils.inRange(codePoint, 0x1f1e6, 0x1f1ff,
                TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE);
    }

    private static boolean isKeyCap(int codePoint) {
        return codePoint == 0x20e3;
    }

}
