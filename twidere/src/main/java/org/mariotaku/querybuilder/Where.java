/**
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
 */

package org.mariotaku.querybuilder;

import org.mariotaku.querybuilder.Columns.Column;

import java.util.Locale;

public class Where implements SQLLang {
	private final String expr;

	public Where(final String expr) {
		this.expr = expr;
	}

	@Override
	public String getSQL() {
		return expr;
	}

	public static Where and(final Where... expressions) {
		return new Where(toExpr(expressions, "AND"));
	}

	public static Where equals(final Column l, final Column r) {
		return new Where(String.format(Locale.US, "%s = %s", l.getSQL(), r.getSQL()));
	}

	public static Where equals(final Column l, final long r) {
		return new Where(String.format(Locale.US, "%s = %d", l.getSQL(), r));
	}

	public static Where equals(final Column l, final String r) {
		return new Where(String.format(Locale.US, "%s = '%s'", l.getSQL(), r));
	}

	public static Where equals(final String l, final long r) {
		return new Where(String.format(Locale.US, "%s = %d", l, r));
	}

	public static Where in(final Column column, final Selectable in) {
		return new Where(String.format("%s IN(%s)", column.getSQL(), in.getSQL()));
	}

	public static Where notEquals(final String l, final long r) {
		return new Where(String.format(Locale.US, "%s != %d", l, r));
	}

	public static Where notEquals(final String l, final String r) {
		return new Where(String.format("%s != %s", l, r));
	}

	public static Where notIn(final Column column, final Selectable in) {
		return new Where(String.format("%s NOT IN(%s)", column.getSQL(), in.getSQL()));
	}

	public static Where notNull(final Column column) {
		return new Where(String.format("%s NOT NULL", column.getSQL()));
	}

	public static Where or(final Where... expressions) {
		return new Where(toExpr(expressions, "OR"));
	}

	private static String toExpr(final Where[] array, final String token) {
		final StringBuilder builder = new StringBuilder();
		builder.append('(');
		final int length = array.length;
		for (int i = 0; i < length; i++) {
			if (i > 0) {
				builder.append(String.format(" %s ", token));
			}
			builder.append(array[i].getSQL());
		}
		builder.append(')');
		return builder.toString();
	}
}
