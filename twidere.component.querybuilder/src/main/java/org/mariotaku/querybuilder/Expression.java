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

public class Expression implements SQLLang {
    private final String expr;

    public Expression(final String expr) {
        this.expr = expr;
    }

    public Expression(SQLLang lang) {
        this(lang.getSQL());
    }

    public static Expression and(final Expression... expressions) {
        return new Expression(toExpr(expressions, "AND"));
    }

    public static Expression equals(final Column l, final Column r) {
        return new Expression(String.format(Locale.ROOT, "%s = %s", l.getSQL(), r.getSQL()));
    }

    public static Expression equals(final Column l, final Selectable r) {
        return new Expression(String.format(Locale.ROOT, "%s = (%s)", l.getSQL(), r.getSQL()));
    }

    public static Expression equals(final String l, final Selectable r) {
        return new Expression(String.format(Locale.ROOT, "%s = (%s)", l, r.getSQL()));
    }

    public static Expression equals(final Column l, final long r) {
        return new Expression(String.format(Locale.ROOT, "%s = %d", l.getSQL(), r));
    }

    public static Expression equals(final Column l, final String r) {
        return new Expression(String.format(Locale.ROOT, "%s = '%s'", l.getSQL(), r));
    }

    public static Expression equals(final String l, final long r) {
        return new Expression(String.format(Locale.ROOT, "%s = %d", l, r));
    }

    public static Expression greaterThan(final String l, final long r) {
        return new Expression(String.format(Locale.ROOT, "%s > %d", l, r));
    }

    public static Expression in(final Column column, final Selectable in) {
        return new Expression(String.format("%s IN(%s)", column.getSQL(), in.getSQL()));
    }

    public static Expression notEquals(final String l, final long r) {
        return new Expression(String.format(Locale.ROOT, "%s != %d", l, r));
    }

    public static Expression notEquals(final String l, final String r) {
        return new Expression(String.format("%s != %s", l, r));
    }

    public static Expression notIn(final Column column, final Selectable in) {
        return new Expression(String.format("%s NOT IN(%s)", column.getSQL(), in.getSQL()));
    }

    public static Expression notNull(final Column column) {
        return new Expression(String.format("%s NOT NULL", column.getSQL()));
    }

    public static Expression or(final Expression... expressions) {
        return new Expression(toExpr(expressions, "OR"));
    }

    private static String toExpr(final Expression[] array, final String token) {
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

    public static Expression equalsArgs(String l) {
        return new Expression(String.format(Locale.ROOT, "%s = ?", l));
    }

    public static Expression isNull(Column column) {
        return new Expression(String.format(Locale.ROOT, "%s IS NULL", column.getSQL()));
    }

    public static Expression greaterThan(Column column1, Column column2) {
        return new Expression(String.format(Locale.ROOT, "%s > %s", column1.getSQL(), column2.getSQL()));
    }

    public static Expression likeRaw(final Column column, final String pattern, final String escape) {
        return new Expression(String.format(Locale.ROOT, "%s LIKE %s ESCAPE '%s'", column.getSQL(), pattern, escape));
    }



    public static Expression like(final Column column, final SQLLang expression) {
        return new Expression(String.format(Locale.ROOT, "%s LIKE %s", column.getSQL(), expression.getSQL()));
    }


    public static Expression likeRaw(final Column column, final String pattern) {
        return new Expression(String.format(Locale.ROOT, "%s LIKE %s", column.getSQL(), pattern));
    }


    @Override
    public String getSQL() {
        return expr;
    }
}
