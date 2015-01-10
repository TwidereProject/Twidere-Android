/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.escape;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.toHtml;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.unescape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.util.Log;

public class HtmlBuilder {

	private static final String LOGTAG = "HtmlBuilder";

	private final String orig;
	private final String[] array;
	private final int string_length;
	private final boolean throw_exceptions, source_is_escaped, should_re_escape;

	private final ArrayList<LinkSpec> links = new ArrayList<LinkSpec>();

	public HtmlBuilder(final String source, final boolean strict, final boolean is_escaped, final boolean re_escape) {
		if (source == null) throw new NullPointerException();
		orig = source;
		array = ArrayUtils.toStringArray(source);
		throw_exceptions = strict;
		source_is_escaped = is_escaped;
		should_re_escape = re_escape;
		string_length = array.length;
	}

	public boolean addLink(final String link, final String display, final int start, final int end) {
		return addLink(link, display, start, end, false);
	}

	public boolean addLink(final String link, final String display, final int start, final int end,
			final boolean display_is_html) {
		if (start < 0 || end < 0 || start > end || end > string_length) {
			final String message = String.format("text:%s, length:%d, start:%d, end:%d", orig, string_length, start,
					end);
			if (throw_exceptions) throw new StringIndexOutOfBoundsException(message);
			Log.e(LOGTAG, message);
			return false;
		}
		if (hasLink(start, end)) {
			final String message = String.format(
					"link already added in this range! text:%s, link:%s, display:%s, start:%d, end:%d", orig, link,
					display, start, end);
			if (throw_exceptions) throw new IllegalArgumentException(message);
			Log.e(LOGTAG, message);
			return false;
		}
		return links.add(new LinkSpec(link, display, start, end, display_is_html));
	}

	public String build() {
		if (links.size() == 0) return escapeSource(ArrayUtils.mergeArrayToString(array));
		Collections.sort(links);
		final StringBuilder builder = new StringBuilder();
		final int links_size = links.size();
		for (int i = 0; i < links_size; i++) {
			final LinkSpec spec = links.get(i);
			if (spec == null) {
				continue;
			}
			final int start = spec.start, end = spec.end;
			if (i == 0) {
				if (start >= 0 && start <= string_length) {
					builder.append(escapeSource(ArrayUtils.mergeArrayToString(ArrayUtils.subArray(array, 0, start))));
				}
			} else if (i > 0) {
				final int last_end = links.get(i - 1).end;
				if (last_end >= 0 && last_end <= start && start <= string_length) {
					builder.append(escapeSource(ArrayUtils.mergeArrayToString(ArrayUtils.subArray(array, last_end,
							start))));
				}
			}
			builder.append("<a href=\"" + spec.link + "\">");
			if (start >= 0 && start <= end && end <= string_length) {
				builder.append(!isEmpty(spec.display) ? spec.display_is_html ? spec.display : toHtml(spec.display)
						: spec.link);
			}
			builder.append("</a>");
			if (i == links.size() - 1 && end >= 0 && end <= string_length) {
				builder.append(escapeSource(ArrayUtils.mergeArrayToString(ArrayUtils
						.subArray(array, end, string_length))));
			}
		}
		return builder.toString();
	}

	public boolean hasLink(final int start, final int end) {
		for (final LinkSpec spec : links) {
			if (start >= spec.start && start <= spec.end || end >= spec.start && end <= spec.end) return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "HtmlBuilder{array=" + Arrays.toString(array) + ", string_length=" + string_length + ", strict="
				+ throw_exceptions + ", source_is_escaped" + source_is_escaped + ", links=" + links + "}";
	}

	private String escapeSource(final String string) {
		final String escaped = source_is_escaped ? string : escape(string);
		return should_re_escape ? escape(unescape(escaped)) : escaped;
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
		public int compareTo(final LinkSpec that) {
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
