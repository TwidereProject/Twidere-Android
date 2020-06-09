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

import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.URLSpan
import org.attoparser.ParseException
import org.attoparser.config.ParseConfiguration
import org.attoparser.simple.AbstractSimpleMarkupHandler
import org.attoparser.simple.SimpleMarkupParser
import java.util.*

/**
 * Created by mariotaku on 15/11/4.
 */
object HtmlSpanBuilder {

    private val PARSER = SimpleMarkupParser(ParseConfiguration.htmlConfiguration())

    @Throws(HtmlParseException::class)
    fun fromHtml(html: String, processor: SpanProcessor? = null): Spannable {
        val handler = HtmlSpanHandler(processor)
        try {
            PARSER.parse(html, handler)
        } catch (e: ParseException) {
            throw HtmlParseException(e)
        }

        return handler.text
    }

    fun fromHtml(html: String?, fallback: CharSequence?, processor: SpanProcessor? = null): CharSequence? {
        if (html == null) return fallback
        return try {
            fromHtml(html, processor)
        } catch (e: HtmlParseException) {
            fallback
        }

    }

    private fun applyTag(sb: SpannableStringBuilder, start: Int, end: Int, info: TagInfo,
            processor: SpanProcessor?) {
        if (processor?.applySpan(sb, start, end, info) == true) return
        if (info.nameLower == "br") {
            sb.append('\n')
        } else {
            val span = createSpan(info) ?: return
            sb.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun createSpan(info: TagInfo): Any? {
        when (info.nameLower) {
            "a" -> {
                val href = info.getAttribute("href") ?: return null
                return URLSpan(href)
            }
            "b", "strong" -> {
                return StyleSpan(Typeface.BOLD)
            }
            "em", "cite", "dfn", "i" -> {
                return StyleSpan(Typeface.ITALIC)
            }
        }
        return null
    }

    private fun lastIndexOfTag(info: List<TagInfo>, name: String): Int {
        return info.indexOfLast { it.name.equals(name, ignoreCase = true) }
    }

    interface SpanProcessor {

        /**
         * @param text Text before content in [buffer] appended
         * @param buffer Raw html buffer
         * @param start Start index of text to append in [buffer]
         * @param len Length of text to append in [buffer]
         */
        fun appendText(text: Editable, buffer: CharArray, start: Int, len: Int, info: TagInfo?): Boolean = false

        /**
         * @param text Text to apply span from [info]
         * @param start Start index for applying span
         * @param end End index for applying span
         * @param info Tag info
         */
        fun applySpan(text: Editable, start: Int, end: Int, info: TagInfo): Boolean = false

    }

    data class TagInfo(val start: Int, val name: String, val attributes: Map<String, String>?,
            val parent: TagInfo? = null) {

        val nameLower = name.toLowerCase(Locale.US)

        fun getAttribute(attr: String): String? {
            return attributes?.get(attr)
        }
    }

    private class HtmlParseException : RuntimeException {

        internal constructor() : super()

        internal constructor(detailMessage: String) : super(detailMessage)
        internal constructor(detailMessage: String, throwable: Throwable) : super(detailMessage, throwable)


        internal constructor(throwable: Throwable) : super(throwable)

    }

    private class HtmlSpanHandler(
            val processor: SpanProcessor?
    ) : AbstractSimpleMarkupHandler() {

        private val sb = SpannableStringBuilder()
        private val tagStack = ArrayList<TagInfo>()
        private var lastTag: TagInfo? = null

        override fun handleText(buffer: CharArray, offset: Int, len: Int, line: Int, col: Int) {
            var cur = offset
            val lastTag = this.lastTag
            while (cur < offset + len) {
                // Find first line break
                var lineBreakIndex = cur
                while (lineBreakIndex < offset + len) {
                    if (buffer[lineBreakIndex] == '\n') break
                    lineBreakIndex++
                }
                if (processor?.appendText(sb, buffer, cur, lineBreakIndex - cur, lastTag) != true) {
                    sb.append(HtmlEscapeHelper.unescape(String(buffer, cur, lineBreakIndex - cur)))
                }
                cur = lineBreakIndex + 1
            }
            this.lastTag = null
        }

        override fun handleCloseElement(elementName: String, line: Int, col: Int) {
            val lastIndex = lastIndexOfTag(tagStack, elementName)
            if (lastIndex == -1) return
            val info = tagStack[lastIndex]
            applyTag(sb, info.start, sb.length, info, processor)
            tagStack.removeAt(lastIndex)
            lastTag = info
        }

        override fun handleOpenElement(elementName: String, attributes: Map<String, String>?,
                line: Int, col: Int) {
            val info = TagInfo(sb.length, elementName, attributes, tagStack.lastOrNull())
            tagStack.add(info)

            // Mastodon case, insert 2 breaks between two <p> tag
            if ("p" == info.nameLower && "p" == lastTag?.nameLower) {
                sb.append("\n\n")
            }
        }

        @Throws(ParseException::class)
        override fun handleStandaloneElement(elementName: String, attributes: Map<String, String>?,
                minimized: Boolean, line: Int, col: Int) {
            val info = TagInfo(sb.length, elementName, attributes, tagStack.lastOrNull())
            applyTag(sb, info.start, sb.length, info, processor)
        }

        val text: Spannable
            get() = sb
    }

}
