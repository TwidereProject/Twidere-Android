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

public class Columns implements Selectable {

    private final AbsColumn[] columns;

    public Columns(String... columns) {
        this(Columns.fromStrings(columns));
    }

    public Columns(final AbsColumn... columns) {
        this.columns = columns;
    }

    private static Column[] fromStrings(String... columnsString) {
        final Column[] columns = new Column[columnsString.length];
        for (int i = 0, j = columnsString.length; i < j; i++) {
            columns[i] = new Column(columnsString[i]);
        }
        return columns;
    }

    @Override
    public String getSQL() {
        return Utils.toString(columns, ',', true);
    }

    public abstract static class AbsColumn implements Selectable {

    }

    public static class AllColumn extends AbsColumn {

        private final Table table;

        public AllColumn() {
            this(null);
        }

        public AllColumn(final Table table) {
            this.table = table;
        }

        @Override
        public String getSQL() {
            return table != null ? table.getSQL() + ".*" : "*";
        }

    }

    public static class Column extends AbsColumn {

        private final Table table;
        private final String columnName, alias;

        public Column(final String columnName) {
            this(null, columnName, null);
        }

        public Column(final String columnName, final String alias) {
            this(null, columnName, alias);
        }

        public Column(final Table table, final String columnName) {
            this(table, columnName, null);
        }

        public Column(final Table table, final String columnName, final String alias) {
            if (columnName == null) throw new IllegalArgumentException("");
            this.table = table;
            this.columnName = columnName;
            this.alias = alias;
        }

        @Override
        public String getSQL() {
            final String col = table != null ? table.getSQL() + "." + columnName : columnName;
            return alias != null ? col + " AS " + alias : col;
        }
    }

}
