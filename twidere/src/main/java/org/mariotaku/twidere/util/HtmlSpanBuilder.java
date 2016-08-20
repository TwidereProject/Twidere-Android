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

package org.mariotaku.twidere.util;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;

import org.apache.commons.lang3.StringUtils;
import org.attoparser.ParseException;
import org.attoparser.config.ParseConfiguration;
import org.attoparser.simple.AbstractSimpleMarkupHandler;
import org.attoparser.simple.SimpleMarkupParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by mariotaku on 15/11/4.
 */
public class HtmlSpanBuilder {

    private static final SimpleMarkupParser PARSER = new SimpleMarkupParser(ParseConfiguration.htmlConfiguration());

    private HtmlSpanBuilder() {
    }

    public static Spannable fromHtml(String html) throws HtmlParseException {
        final HtmlSpanHandler handler = new HtmlSpanHandler();
        try {
            PARSER.parse(html, handler);
        } catch (ParseException e) {
            throw new HtmlParseException(e);
        }
        return handler.getText();
    }

    public static CharSequence fromHtml(String html, CharSequence fallback) {
        try {
            return fromHtml(html);
        } catch (HtmlParseException e) {
            return fallback;
        }
    }

    private static void applyTag(SpannableStringBuilder sb, int start, int end, TagInfo info) {
        if (info.name.equalsIgnoreCase("br")) {
            sb.append('\n');
        } else {
            final Object span = createSpan(info);
            if (span == null) return;
            sb.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private static Object createSpan(TagInfo info) {
        switch (info.name.toLowerCase(Locale.US)) {
            case "a": {
                return new URLSpan(info.getAttribute("href"));
            }
            case "b":
            case "strong": {
                return new StyleSpan(Typeface.BOLD);
            }
            case "em":
            case "cite":
            case "dfn":
            case "i": {
                return new StyleSpan(Typeface.ITALIC);
            }
        }
        return null;
    }

    private static int lastIndexOfTag(List<TagInfo> info, String name) {
        for (int i = info.size() - 1; i >= 0; i--) {
            if (StringUtils.equals(info.get(i).name, name)) {
                return i;
            }
        }
        return -1;
    }

    public static class HtmlParseException extends RuntimeException {
        public HtmlParseException() {
            super();
        }

        public HtmlParseException(String detailMessage) {
            super(detailMessage);
        }

        public HtmlParseException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public HtmlParseException(Throwable throwable) {
            super(throwable);
        }
    }

    static class TagInfo {
        final int start;
        final String name;
        final Map<String, String> attributes;

        public TagInfo(int start, String name, Map<String, String> attributes) {
            this.start = start;
            this.name = name;
            this.attributes = attributes;
        }

        public String getAttribute(String attr) {
            return attributes.get(attr);
        }
    }

    static class HtmlSpanHandler extends AbstractSimpleMarkupHandler {
        private final SpannableStringBuilder sb;
        List<TagInfo> tagInfo;

        public HtmlSpanHandler() {
            sb = new SpannableStringBuilder();
            tagInfo = new ArrayList<>();
        }

        @Override
        public void handleText(char[] buffer, int offset, int len, int line, int col) {
            sb.append(HtmlEscapeHelper.unescape(new String(buffer, offset, len)));
        }

        @Override
        public void handleCloseElement(String elementName, int line, int col) {
            final int lastIndex = lastIndexOfTag(tagInfo, elementName);
            if (lastIndex != -1) {
                TagInfo info = tagInfo.get(lastIndex);
                applyTag(sb, info.start, sb.length(), info);
                tagInfo.remove(lastIndex);
            }
        }

        @Override
        public void handleOpenElement(String elementName, Map<String, String> attributes, int line, int col) {
            tagInfo.add(new TagInfo(sb.length(), elementName, attributes));
        }

        public Spannable getText() {
            return sb;
        }
    }
}
