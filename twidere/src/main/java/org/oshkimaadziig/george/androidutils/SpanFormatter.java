/*
 * Twidere - Twitter client for Android
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

package org.oshkimaadziig.george.androidutils;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides {@link String#format} style functions that work with {@link Spanned} strings and preserve formatting.
 *
 * @author George T. Steel
 */
@SuppressWarnings("IfCanBeSwitch")
public class SpanFormatter {
    public static final Pattern FORMAT_SEQUENCE = Pattern.compile("%([0-9]+\\$|<?)([^a-zA-z%]*)([[a-zA-Z%]&&[^tT]]|[tT][a-zA-Z])");

    private SpanFormatter() {
    }

    /**
     * Version of {@link String#format(String, Object...)} that works on {@link Spanned} strings to preserve rich text formatting.
     * Both the {@code format} as well as any {@code %s args} can be Spanned and will have their formatting preserved.
     * Due to the way {@link Spannable}s work, any argument's spans will can only be included <b>once</b> in the result.
     * Any duplicates will appear as text only.
     *
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args   the list of arguments passed to the formatter. If there are
     *               more arguments than required by {@code format},
     *               additional arguments are ignored.
     * @return the formatted string (with spans).
     */
    public static SpannedString format(CharSequence format, Object... args) {
        return format(Locale.getDefault(), format, args);
    }

    /**
     * Version of {@link String#format(Locale, String, Object...)} that works on {@link Spanned} strings to preserve rich text formatting.
     * Both the {@code format} as well as any {@code %s args} can be Spanned and will have their formatting preserved.
     * Due to the way {@link Spannable}s work, any argument's spans will can only be included <b>once</b> in the result.
     * Any duplicates will appear as text only.
     *
     * @param locale the locale to apply; {@code null} value means no localization.
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args   the list of arguments passed to the formatter.
     * @return the formatted string (with spans).
     * @see String#format(Locale, String, Object...)
     */
    public static SpannedString format(Locale locale, CharSequence format, Object... args) {
        SpannableStringBuilder out = new SpannableStringBuilder(format);

        int i = 0;
        int argAt = -1;

        while (i < out.length()) {
            Matcher m = FORMAT_SEQUENCE.matcher(out);
            if (!m.find(i)) break;
            i = m.start();
            int exprEnd = m.end();

            String argTerm = m.group(1);
            String modTerm = m.group(2);
            String typeTerm = m.group(3);

            CharSequence cookedArg;

            if (typeTerm.equals("%")) {
                cookedArg = "%";
            } else if (typeTerm.equals("%")) {
                cookedArg = "\n";
            } else {
                int argIdx = 0;
                if (argTerm.equals("")) argIdx = ++argAt;
                else if (argTerm.equals("<")) argIdx = argAt;
                else argIdx = Integer.parseInt(argTerm.substring(0, argTerm.length() - 1)) - 1;

                Object argItem = args[argIdx];

                if (typeTerm.equals("s") && argItem instanceof Spanned) {
                    cookedArg = (Spanned) argItem;
                } else {
                    cookedArg = String.format(locale, "%" + modTerm + typeTerm, argItem);
                }
            }

            out.replace(i, exprEnd, cookedArg);
            i += cookedArg.length();
        }

        return new SpannedString(out);
    }
}