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

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlLinkExtractor {

	private final Pattern patternTag, patternLink;
	private Matcher matcherTag, matcherLink;

	private static final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
	private static final String HTML_A_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";

	public HtmlLinkExtractor() {
		patternTag = Pattern.compile(HTML_A_TAG_PATTERN);
		patternLink = Pattern.compile(HTML_A_HREF_TAG_PATTERN);
	}

	/**
	 * Validate html with regular expression
	 * 
	 * @param html html content for validation
	 * @return Vector links and link text
	 */
	public Vector<HtmlLink> grabLinks(final String html) {
		final Vector<HtmlLink> result = new Vector<HtmlLink>();
		matcherTag = patternTag.matcher(html);
		while (matcherTag.find()) {
			final String href = matcherTag.group(1); // href
			final String linkText = matcherTag.group(2); // link text
			matcherLink = patternLink.matcher(href);
			while (matcherLink.find()) {
				final String link = matcherLink.group(1); // link
				final HtmlLink obj = new HtmlLink(link, linkText);
				result.add(obj);
			}
		}
		return result;
	}

	public static class HtmlLink {

		private final String link;
		private final String text;

		private HtmlLink(final String link, final String text) {
			this.link = replaceInvalidChar(link);
			this.text = text;
		};

		public String getLink() {
			return link;
		}

		public String getLinkText() {
			return text;
		}

		private static String replaceInvalidChar(String link) {
			link = link.replaceAll("'", "");
			link = link.replaceAll("\"", "");
			return link;
		}

	}
}
