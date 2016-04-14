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

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CodePointTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;

import java.io.IOException;
import java.io.Writer;

public class HtmlEscapeHelper {

    public static final AggregateTranslator ESCAPE_HTML = new AggregateTranslator(StringEscapeUtils.ESCAPE_HTML4,
            new UnicodeControlCharacterToHtmlTranslator());
    public static final LookupTranslator ESCAPE_BASIC = new LookupTranslator(EntityArrays.BASIC_ESCAPE());

    private HtmlEscapeHelper() {
    }

    public static String escape(final CharSequence text) {
        if (text == null) return null;
        return ESCAPE_HTML.translate(text);
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

    public static String escapeBasic(CharSequence text) {
        return ESCAPE_BASIC.translate(text);
    }

    private static class UnicodeControlCharacterToHtmlTranslator extends CodePointTranslator {

        @Override
        public boolean translate(int codePoint, Writer out) throws IOException {
            if (Character.isISOControl(codePoint)) {
                out.append("&#x");
                final char[] chars = Character.toChars(codePoint);
                for (char c : chars) {
                    out.append(Integer.toHexString(c));
                }
                out.append(';');
                return true;
            }
            return false;
        }
    }
}
