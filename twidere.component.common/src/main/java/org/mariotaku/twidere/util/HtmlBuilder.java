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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.escape;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.toHtml;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.unescape;

public class HtmlBuilder {

    private final String source;
    private final int[] codePoints;
    private final int codePointsLength;
    private final boolean throwExceptions, sourceIsEscaped, shouldReEscape;

    private final ArrayList<LinkSpec> links = new ArrayList<>();

    public HtmlBuilder(final String source, final boolean strict, final boolean sourceIsEscaped,
                       final boolean shouldReEscape) {
        if (source == null) throw new NullPointerException();
        this.source = source;
        final int length = source.length();
        final int[] codepointsTemp = new int[length];
        int codePointsLength = 0;
        for (int offset = 0; offset < length; ) {
            final int codepoint = source.codePointAt(offset);
            codepointsTemp[codePointsLength++] = codepoint;
            offset += Character.charCount(codepoint);
        }
        codePoints = new int[codePointsLength];
        System.arraycopy(codepointsTemp, 0, codePoints, 0, codePointsLength);
        throwExceptions = strict;
        this.sourceIsEscaped = sourceIsEscaped;
        this.shouldReEscape = shouldReEscape;
        this.codePointsLength = codePointsLength;
    }

    public boolean addLink(final String link, final String display, final int start, final int end) {
        return addLink(link, display, start, end, false);
    }

    public boolean addLink(final String link, final String display, final int start, final int end,
                           final boolean display_is_html) {
        if (start < 0 || end < 0 || start > end || end > codePointsLength) {
            final String message = String.format(Locale.US, "text:%s, length:%d, start:%d, end:%d", source,
                    codePointsLength, start, end);
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
        return links.add(new LinkSpec(link, display, start, end, display_is_html));
    }

    public String build() {
        if (links.isEmpty()) return escapeSource(source);
        Collections.sort(links);
        final StringBuilder builder = new StringBuilder();
        final int linksSize = links.size();
        for (int i = 0; i < linksSize; i++) {
            final LinkSpec spec = links.get(i);
            if (spec == null) {
                continue;
            }
            final int start = spec.start, end = spec.end;
            if (i == 0) {
                if (start >= 0 && start <= codePointsLength) {
                    appendSource(builder, 0, start);
                }
            } else if (i > 0) {
                final int lastEnd = links.get(i - 1).end;
                if (lastEnd >= 0 && lastEnd <= start && start <= codePointsLength) {
                    appendSource(builder, lastEnd, start);
                }
            }
            builder.append("<a href=\"");
            builder.append(spec.link);
            builder.append("\">");
            if (start >= 0 && start <= end && end <= codePointsLength) {
                builder.append(!isEmpty(spec.display) ? spec.display_is_html ? spec.display : toHtml(spec.display)
                        : spec.link);
            }
            builder.append("</a>");
            if (i == linksSize - 1 && end >= 0 && end <= codePointsLength) {
                appendSource(builder, end, codePointsLength);
            }
        }
        return builder.toString();
    }

    public boolean hasLink(final int start, final int end) {
        for (final LinkSpec spec : links) {
            if (start >= spec.start && start <= spec.end || end >= spec.start && end <= spec.end)
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "HtmlBuilder{orig=" + source + ", codePoints=" + Arrays.toString(codePoints) + ", string_length="
                + codePointsLength + ", throw_exceptions=" + throwExceptions + ", source_is_escaped=" + sourceIsEscaped
                + ", should_re_escape=" + shouldReEscape + ", links=" + links + "}";
    }

    private void appendSource(final StringBuilder builder, final int start, final int end) {
        if (sourceIsEscaped == shouldReEscape) {
            for (int i = start; i < end; i++) {
                builder.appendCodePoint(codePoints[i]);
            }
        } else if (shouldReEscape) {
            builder.append(escape(subString(start, end)));
        } else {
            builder.append(unescape(subString(start, end)));
        }
    }

    private String escapeSource(final String source) {
        if (sourceIsEscaped == shouldReEscape) return source;
        return shouldReEscape ? escape(source) : unescape(source);
    }

    private String subString(final int start, final int end) {
        final StringBuilder sb = new StringBuilder();
        for (int i = start; i < end; i++) {
            sb.appendCodePoint(codePoints[i]);
        }
        return sb.toString();
    }

    static final class LinkSpec implements Comparable<LinkSpec> {

        final String link, display;
        final int start, end;
        final boolean display_is_html;

        LinkSpec(final String link, final String display, final int start, final int end, final boolean display_is_html) {
            this.link = link;
            this.display = display;
            this.start = start;
            this.end = end;
            this.display_is_html = display_is_html;
        }

        @Override
        public int compareTo(@NonNull final LinkSpec that) {
            return start - that.start;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (!(obj instanceof LinkSpec)) return false;
            final LinkSpec other = (LinkSpec) obj;
            if (display == null) {
                if (other.display != null) return false;
            } else if (!display.equals(other.display)) return false;
            if (display_is_html != other.display_is_html) return false;
            if (end != other.end) return false;
            if (link == null) {
                if (other.link != null) return false;
            } else if (!link.equals(other.link)) return false;
            if (start != other.start) return false;
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (display == null ? 0 : display.hashCode());
            result = prime * result + (display_is_html ? 1231 : 1237);
            result = prime * result + end;
            result = prime * result + (link == null ? 0 : link.hashCode());
            result = prime * result + start;
            return result;
        }

        @Override
        public String toString() {
            return "LinkSpec{link=" + link + ", display=" + display + ", start=" + start + ", end=" + end
                    + ", display_is_html=" + display_is_html + "}";
        }
    }

}
