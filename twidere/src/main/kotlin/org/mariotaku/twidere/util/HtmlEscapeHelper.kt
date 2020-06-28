/*
 * 				Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util

import org.apache.commons.text.translate.AggregateTranslator
import org.apache.commons.text.translate.CodePointTranslator
import org.apache.commons.text.translate.EntityArrays
import org.apache.commons.text.translate.LookupTranslator
import org.apache.commons.text.translate.NumericEntityUnescaper
import java.io.IOException
import java.io.Writer

object HtmlEscapeHelper {

    val ESCAPE_BASIC = LookupTranslator(EntityArrays.BASIC_ESCAPE)

    val ESCAPE_HTML = AggregateTranslator(
            LookupTranslator(EntityArrays.BASIC_ESCAPE),
            LookupTranslator(EntityArrays.ISO8859_1_ESCAPE),
            LookupTranslator(EntityArrays.HTML40_EXTENDED_ESCAPE),
            UnicodeControlCharacterToHtmlTranslator()
    )

    val UNESCAPE_HTML = AggregateTranslator(
            LookupTranslator(EntityArrays.BASIC_UNESCAPE),
            LookupTranslator(EntityArrays.ISO8859_1_UNESCAPE),
            LookupTranslator(EntityArrays.HTML40_EXTENDED_UNESCAPE),
            LookupTranslator(EntityArrays.APOS_UNESCAPE),
            IgnoreErrorNumericEntityUnescaper()
    )

    fun escape(text: CharSequence): String {
        return ESCAPE_HTML.translate(text)
    }

    fun toPlainText(string: String): String {
        return unescape(string.replace("<br/>", "\n").replace("<!--.*?-->|<[^>]+>".toRegex(), ""))
    }

    fun unescape(string: String): String {
        return UNESCAPE_HTML.translate(string)
    }

    fun escapeBasic(text: CharSequence): String {
        return ESCAPE_BASIC.translate(text)
    }

    private class UnicodeControlCharacterToHtmlTranslator : CodePointTranslator() {

        @Throws(IOException::class)
        override fun translate(codePoint: Int, out: Writer): Boolean {
            if (Character.isISOControl(codePoint)) {
                out.append("&#x")
                val chars = Character.toChars(codePoint)
                for (c in chars) {
                    out.append(Integer.toHexString(c.toInt()))
                }
                out.append(';')
                return true
            }
            return false
        }
    }

    private class IgnoreErrorNumericEntityUnescaper : NumericEntityUnescaper() {
        override fun translate(input: CharSequence?, index: Int, out: Writer?): Int {
            return try {
                super.translate(input, index, out)
            } catch (e: IllegalArgumentException) {
                // Ignore unsupported code points
                0
            }
        }
    }
}
