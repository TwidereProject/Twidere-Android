package org.mariotaku.twidere.util;

import org.mariotaku.twidere.util.apache.commons.lang3.StringEscapeUtils;

public class HtmlEscapeHelper {

	public static String escape(final String string) {
		if (string == null) return null;
		return StringEscapeUtils.escapeHtml4(string);
	}

	public static String toHtml(final String string) {
		if (string == null) return null;
		return escape(string).replace("\n", "<br/>");
	}

	public static String toPlainText(final String string) {
		if (string == null) return null;
		return unescape(string.replace("<br/>", "\n").replaceAll("<!--.*?-->|<[^>]+>", ""));
	}

	public static String unescape(final String string) {
		if (string == null) return null;
		return StringEscapeUtils.unescapeHtml4(string);
	}

}
