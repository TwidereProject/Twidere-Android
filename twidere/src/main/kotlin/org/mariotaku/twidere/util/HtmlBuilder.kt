/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import org.mariotaku.commons.text.CodePointArray
import org.mariotaku.twidere.model.SpanItem
import java.util.*

class HtmlBuilder(
        private val source: CodePointArray,
        private val throwExceptions: Boolean,
        private val sourceIsEscaped: Boolean,
        private val shouldReEscape: Boolean
) {

    private val sourceLength = source.length()

    private val spanSpecs = ArrayList<SpanSpec>()

    constructor(source: String, strict: Boolean, sourceIsEscaped: Boolean, shouldReEscape: Boolean)
            : this(CodePointArray(source), strict, sourceIsEscaped, shouldReEscape)

    fun addLink(link: String, display: String, start: Int, end: Int,
            displayIsHtml: Boolean = false): Boolean {
        if (start < 0 || end < 0 || start > end || end > sourceLength) {
            if (throwExceptions) {
                val message = "text:$source, length:$sourceLength, start:$start, end:$end"
                throw StringIndexOutOfBoundsException(message)
            }
            return false
        }
        if (hasLink(start, end)) {
            if (throwExceptions) {
                val message = "link already added in this range! text:$source, link:$link, display:$display, start:$start, end:end"
                throw IllegalArgumentException(message)
            }
            return false
        }
        return spanSpecs.add(LinkSpec(link, display, start, end, displayIsHtml))
    }

    fun buildWithIndices(): Pair<String, Array<SpanItem>> {
        if (spanSpecs.isEmpty()) return Pair(escapeSource(), emptyArray())
        spanSpecs.sort()
        val sb = StringBuilder()
        val linksSize = spanSpecs.size
        val items = arrayOfNulls<SpanItem>(linksSize)
        for (i in 0 until linksSize) {
            val spec = spanSpecs[i]
            val start = spec.start
            val end = spec.end
            if (i == 0) {
                if (start in 0..sourceLength) {
                    appendSource(sb, 0, start, false, sourceIsEscaped)
                }
            } else if (i > 0) {
                val lastEnd = spanSpecs[i - 1].end
                if (lastEnd in 0..start && start <= sourceLength) {
                    appendSource(sb, lastEnd, start, false, sourceIsEscaped)
                }
            }
            val spanStart = sb.length
            if (start in 0..end && end <= sourceLength) {
                spec.appendTo(sb)
            }
            val item = SpanItem()
            item.start = spanStart
            item.end = sb.length
            item.orig_start = start
            item.orig_end = end
            if (spec is LinkSpec) {
                item.link = spec.link
            }
            items[i] = item
            if (i == linksSize - 1 && end >= 0 && end <= sourceLength) {
                appendSource(sb, end, sourceLength, false, sourceIsEscaped)
            }
        }
        return Pair(sb.toString(), items.requireNoNulls())
    }

    fun hasLink(start: Int, end: Int): Boolean {
        for (spec in spanSpecs) {
            val specStart = spec.start
            val specEnd = spec.end
            if (start in specStart..specEnd || end in specStart..specEnd) {
                return true
            }
        }
        return false
    }

    override fun toString(): String {
        return "HtmlBuilder{" +
                ", codePoints=" + source +
                ", codePointsLength=" + sourceLength +
                ", throwExceptions=" + throwExceptions +
                ", sourceIsEscaped=" + sourceIsEscaped +
                ", shouldReEscape=" + shouldReEscape +
                ", links=" + spanSpecs +
                '}'
    }

    private fun appendSource(builder: StringBuilder, start: Int, end: Int, escapeSource: Boolean, sourceEscaped: Boolean) {
        when {
            sourceEscaped == escapeSource -> {
                builder.append(source.substring(start, end), escapeSource, sourceEscaped)
            }
            escapeSource -> {
                builder.append(HtmlEscapeHelper.escape(source.substring(start, end)), true, sourceEscaped)
            }
            else -> {
                builder.append(HtmlEscapeHelper.unescape(source.substring(start, end)), false, sourceEscaped)
            }
        }
    }

    private fun escapeSource(): String {
        val source = this.source.substring(0, this.source.length())
        if (sourceIsEscaped == shouldReEscape) return source
        return if (shouldReEscape) HtmlEscapeHelper.escape(source) else HtmlEscapeHelper.unescape(source)
    }

    private interface SpanSpec : Comparable<SpanSpec> {
        val start: Int
        val end: Int

        override fun compareTo(other: SpanSpec): Int {
            return start - other.start
        }

        fun appendTo(sb: StringBuilder)
    }

    private data class LinkSpec(
            val link: String,
            val display: String?,
            override val start: Int,
            override val end: Int,
            val displayIsHtml: Boolean
    ) : SpanSpec {

        override fun appendTo(sb: StringBuilder) {
            if (display != null) {
                sb.append(display, false, displayIsHtml)
            } else {
                sb.append(link, escapeText = false, textEscaped = false)
            }
        }

    }

    companion object {

        private fun StringBuilder.append(text: String, escapeText: Boolean, textEscaped: Boolean) {
            when {
                textEscaped == escapeText -> {
                    append(text)
                }
                escapeText -> {
                    append(HtmlEscapeHelper.escape(text))
                }
                else -> {
                    append(HtmlEscapeHelper.unescape(text))
                }
            }
        }
    }

}
