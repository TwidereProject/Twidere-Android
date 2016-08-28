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

package org.mariotaku.twidere.util;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import org.mariotaku.commons.text.CodePointArray;
import org.mariotaku.twidere.model.SpanItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.escape;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.unescape;

public class HtmlBuilder {

    private final CodePointArray source;
    private final int sourceLength;
    private final boolean throwExceptions, sourceIsEscaped, shouldReEscape;

    private final ArrayList<SpanSpec> spanSpecs = new ArrayList<>();

    public HtmlBuilder(final String source, final boolean strict, final boolean sourceIsEscaped,
                       final boolean shouldReEscape) {
        this(new CodePointArray(source), strict, sourceIsEscaped, shouldReEscape);
    }

    public HtmlBuilder(final CodePointArray source, final boolean strict, final boolean sourceIsEscaped,
                       final boolean shouldReEscape) {
        if (source == null) throw new NullPointerException();
        this.source = source;
        this.sourceLength = source.length();
        this.throwExceptions = strict;
        this.sourceIsEscaped = sourceIsEscaped;
        this.shouldReEscape = shouldReEscape;
    }

    public boolean addLink(final String link, final String display, final int start, final int end) {
        return addLink(link, display, start, end, false);
    }

    public boolean addLink(final String link, final String display, final int start, final int end,
                           final boolean displayIsHtml) {
        if (start < 0 || end < 0 || start > end || end > sourceLength) {
            final String message = String.format(Locale.US, "text:%s, length:%d, start:%d, end:%d", source,
                    sourceLength, start, end);
            if (throwExceptions) throw new StringIndexOutOfBoundsException(message);
            return false;
        }
        if (hasLink(start, end)) {
            final String message = String.format(Locale.US,
                    "link already added in this range! text:%s, link:%s, display:%s, start:%d, end:%d", source, link,
                    display, start, end);
            if (throwExceptions) throw new IllegalArgumentException(message);
            return false;
        }
        return spanSpecs.add(new LinkSpec(link, display, start, end, displayIsHtml));
    }

    public Pair<String, SpanItem[]> buildWithIndices() {
        if (spanSpecs.isEmpty()) return Pair.create(escapeSource(), new SpanItem[0]);
        Collections.sort(spanSpecs);
        final StringBuilder sb = new StringBuilder();
        final int linksSize = spanSpecs.size();
        SpanItem[] items = new SpanItem[linksSize];
        for (int i = 0; i < linksSize; i++) {
            final SpanSpec spec = spanSpecs.get(i);
            final int start = spec.getStart(), end = spec.getEnd();
            if (i == 0) {
                if (start >= 0 && start <= sourceLength) {
                    appendSource(sb, 0, start, false, sourceIsEscaped);
                }
            } else if (i > 0) {
                final int lastEnd = spanSpecs.get(i - 1).end;
                if (lastEnd >= 0 && lastEnd <= start && start <= sourceLength) {
                    appendSource(sb, lastEnd, start, false, sourceIsEscaped);
                }
            }
            int spanStart = sb.length();
            if (start >= 0 && start <= end && end <= sourceLength) {
                spec.appendTo(sb);
            }
            final SpanItem item = new SpanItem();
            item.start = spanStart;
            item.end = sb.length();
            item.orig_start = start;
            item.orig_end = end;
            if (spec instanceof LinkSpec) {
                item.link = ((LinkSpec) spec).link;
            }
            items[i] = item;
            if (i == linksSize - 1 && end >= 0 && end <= sourceLength) {
                appendSource(sb, end, sourceLength, false, sourceIsEscaped);
            }
        }
        return Pair.create(sb.toString(), items);
    }

    public boolean hasLink(final int start, final int end) {
        for (final SpanSpec spec : spanSpecs) {
            final int specStart = spec.getStart(), specEnd = spec.getEnd();
            if (start >= specStart && start <= specEnd || end >= specStart && end <= specEnd) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "HtmlBuilder{" +
                ", codePoints=" + source +
                ", codePointsLength=" + sourceLength +
                ", throwExceptions=" + throwExceptions +
                ", sourceIsEscaped=" + sourceIsEscaped +
                ", shouldReEscape=" + shouldReEscape +
                ", links=" + spanSpecs +
                '}';
    }

    private void appendSource(final StringBuilder builder, final int start, final int end, boolean escapeSource, boolean sourceEscaped) {
        if (sourceEscaped == escapeSource) {
            append(builder, source.substring(start, end), escapeSource, sourceEscaped);
        } else if (escapeSource) {
            append(builder, escape(source.substring(start, end)), true, sourceEscaped);
        } else {
            append(builder, unescape(source.substring(start, end)), false, sourceEscaped);
        }
    }

    private static void append(final StringBuilder builder, final String text, boolean escapeText, boolean textEscaped) {
        if (textEscaped == escapeText) {
            builder.append(text);
        } else if (escapeText) {
            builder.append(escape(text));
        } else {
            builder.append(unescape(text));
        }
    }

    private String escapeSource() {
        final String source = this.source.substring(0, this.source.length());
        if (sourceIsEscaped == shouldReEscape) return source;
        return shouldReEscape ? escape(source) : unescape(source);
    }

    static abstract class SpanSpec implements Comparable<SpanSpec> {

        final int start, end;

        public final int getStart() {
            return start;
        }

        public final int getEnd() {
            return end;
        }

        public SpanSpec(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public int compareTo(@NonNull final SpanSpec that) {
            return start - that.start;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SpanSpec spanSpec = (SpanSpec) o;

            if (start != spanSpec.start) return false;
            return end == spanSpec.end;

        }

        @Override
        public int hashCode() {
            int result = start;
            result = 31 * result + end;
            return result;
        }

        @Override
        public String toString() {
            return "SpanSpec{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }

        public abstract void appendTo(StringBuilder sb);
    }

    static final class LinkSpec extends SpanSpec {

        final String link, display;

        final boolean displayIsHtml;

        LinkSpec(final String link, final String display, final int start, final int end, final boolean displayIsHtml) {
            super(start, end);
            this.link = link;
            this.display = display;
            this.displayIsHtml = displayIsHtml;
        }


        @Override
        public void appendTo(StringBuilder sb) {
            if (isEmpty(display)) {
                append(sb, link, false, false);
            } else {
                append(sb, display, false, displayIsHtml);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            LinkSpec linkSpec = (LinkSpec) o;

            if (displayIsHtml != linkSpec.displayIsHtml) return false;
            if (link != null ? !link.equals(linkSpec.link) : linkSpec.link != null) return false;
            return display != null ? display.equals(linkSpec.display) : linkSpec.display == null;

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (link != null ? link.hashCode() : 0);
            result = 31 * result + (display != null ? display.hashCode() : 0);
            result = 31 * result + (displayIsHtml ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "LinkSpec{" +
                    "link='" + link + '\'' +
                    ", display='" + display + '\'' +
                    ", displayIsHtml=" + displayIsHtml +
                    "} " + super.toString();
        }
    }

}
