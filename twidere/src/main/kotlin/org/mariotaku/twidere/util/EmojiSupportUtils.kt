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

package org.mariotaku.twidere.util

import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.Spanned
import android.widget.TextView
import org.mariotaku.commons.text.CodePointArray
import org.mariotaku.commons.text.get

import org.mariotaku.twidere.text.style.EmojiSpan
import org.mariotaku.twidere.text.util.EmojiEditableFactory
import org.mariotaku.twidere.text.util.EmojiSpannableFactory

/**
 * Created by mariotaku on 15/12/20.
 */
object EmojiSupportUtils {

    fun initForTextView(textView: TextView) {
        if (textView.isInEditMode) return
        textView.setSpannableFactory(EmojiSpannableFactory(textView))
        textView.setEditableFactory(EmojiEditableFactory(textView))
    }

    fun applyEmoji(manager: ExternalThemeManager, text: Spannable,
                   textStart: Int = 0, textLength: Int = text.length) {
        val emoji = manager.emoji
        if (emoji == null || !emoji.isSupported) return
        val array = CodePointArray(text)
        var arrayIdx = array.length() - 1
        while (arrayIdx >= 0) {
            val codePoint = array[arrayIdx]
            if (isEmoji(codePoint)) {
                val arrayEnd = arrayIdx + 1
                var arrayIdxOffset = 0
                val textIdx = array.indexOfText(codePoint, arrayIdx)
                var textIdxOffset = 0
                var skippedIndex = 0
                if (textIdx == -1 || textIdx < textStart) {
                    arrayIdx--
                    continue
                }
                val textEnd = textIdx + Character.charCount(codePoint)
                if (arrayIdx > 0) {
                    val prevCodePoint = array[arrayIdx - 1]
                    when {
                        isRegionalIndicatorSymbol(codePoint) -> if (isRegionalIndicatorSymbol(prevCodePoint)) {
                            arrayIdxOffset = -1
                            textIdxOffset = -Character.charCount(prevCodePoint)
                            skippedIndex = -1
                        }
                        isModifier(codePoint) -> if (isEmoji(prevCodePoint)) {
                            arrayIdxOffset = -1
                            textIdxOffset = -Character.charCount(prevCodePoint)
                            skippedIndex = -1
                        }
                        isKeyCap(codePoint) -> if (isPhoneNumberSymbol(prevCodePoint)) {
                            arrayIdxOffset = -1
                            textIdxOffset = -Character.charCount(prevCodePoint)
                            skippedIndex = -1
                        }
                        isZeroWidthJoin(prevCodePoint) -> {
                            var notValidControlCount = 0
                            var charCount = 0
                            for (i in arrayIdx - 1 downTo 0) {
                                val cp = array.get(i)
                                charCount += Character.charCount(cp)
                                if (isZeroWidthJoin(cp) || isVariationSelector(cp)) {
                                    // Ignore
                                    notValidControlCount = 0
                                    continue
                                }
                                notValidControlCount++
                                if (notValidControlCount > 1 || i == 0) {
                                    arrayIdxOffset = i - arrayIdx + 1
                                    textIdxOffset = -charCount + Character.charCount(cp)
                                    skippedIndex = i - arrayIdx + 1
                                    break
                                }
                            }
                        }
                    }
                }
                if (textEnd > textStart + textLength) {
                    arrayIdx--
                    continue
                }
                var spans = text.getSpans(textIdx + textIdxOffset, textEnd, EmojiSpan::class.java)
                if (spans.size == 0) {
                    var drawable: Drawable? = emoji.getEmojiDrawableFor(*array[arrayIdx + arrayIdxOffset..arrayEnd])
                    if (drawable == null) {
                        // Not emoji combination, just use fallback
                        textIdxOffset = 0
                        arrayIdxOffset = 0
                        skippedIndex = 0
                        spans = text.getSpans(textIdx + textIdxOffset, textEnd, EmojiSpan::class.java)
                        if (spans.size == 0) {
                            drawable = emoji.getEmojiDrawableFor(*array[arrayIdx + arrayIdxOffset..arrayEnd])
                        }
                    }
                    if (drawable != null) {
                        text.setSpan(EmojiSpan(drawable), textIdx + textIdxOffset, textEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
                arrayIdx += skippedIndex
            }
            arrayIdx--
        }
    }

    private fun isVariationSelector(codePoint: Int): Boolean {
        return codePoint == 0xfe0f
    }

    private fun isZeroWidthJoin(codePoint: Int): Boolean {
        return codePoint == 0x200d
    }

    private fun isPhoneNumberSymbol(codePoint: Int): Boolean {
        return codePoint == 0x0023 || codePoint == 0x002a || codePoint in 0x0030..0x0039
    }

    private fun isModifier(codePoint: Int): Boolean {
        return codePoint in 0x1f3fb..0x1f3ff
    }

    private fun isEmoji(codePoint: Int): Boolean {
        return !Character.isLetterOrDigit(codePoint)
    }

    private fun isRegionalIndicatorSymbol(codePoint: Int): Boolean {
        return codePoint in 0x1f1e6..0x1f1ff
    }

    private fun isKeyCap(codePoint: Int): Boolean {
        return codePoint == 0x20e3
    }

}
