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

package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.Columns;
import org.mariotaku.querybuilder.Expression;
import org.mariotaku.querybuilder.SQLQuery;
import org.mariotaku.querybuilder.Table;

public class SQLCreateIndexQuery implements SQLQuery {

    private boolean unique;
    private boolean createIfNotExists;
    private Table table;
    private String indexName;
    private Columns indexedColumns;
    private Expression where;

    SQLCreateIndexQuery() {

    }

    @Override
    public String getSQL() {
        if (table == null) throw new NullPointerException("Table must not be null!");
        if (indexName == null)
            throw new NullPointerException("SELECT statement must not be null!");
        final StringBuilder sb = new StringBuilder("CREATE");
        if (unique) {
            sb.append(" UNIQUE");
        }
        sb.append(" INDEX");
        if (createIfNotExists) {
            sb.append(" IF NOT EXISTS");
        }
        if (indexedColumns == null)
            throw new NullPointerException("Indexed columns must not be null !");
        sb.append(String.format(" %s ON %s (%s)", indexName, table.getSQL(), indexedColumns.getSQL()));
        if (where != null) {
            sb.append(" WHERE");
            sb.append(where.getSQL());
        }
        return sb.toString();
    }

    public void setIndexedColumns(Columns indexedColumns) {
        this.indexedColumns = indexedColumns;
    }

    public void setWhere(Expression where) {
        this.where = where;
    }

    void setIndexName(final String indexName) {
        this.indexName = indexName;
    }

    void setCreateIfNotExists(final boolean createIfNotExists) {
        this.createIfNotExists = createIfNotExists;
    }

    void setTable(final Table table) {
        this.table = table;
    }

    void setUnique(final boolean unique) {
        this.unique = unique;
    }

    public static final class Builder implements IBuilder<SQLCreateIndexQuery> {

        private final SQLCreateIndexQuery query = new SQLCreateIndexQuery();
        private boolean buildCalled;

        public Builder on(final Table table, Columns indexedColumns) {
            checkNotBuilt();
            query.setTable(table);
            query.setIndexedColumns(indexedColumns);
            return this;
        }

        public Builder name(final String name) {
            checkNotBuilt();
            query.setIndexName(name);
            return this;
        }

        public Builder where(final Expression expression) {
            checkNotBuilt();
            query.setWhere(expression);
            return this;
        }


        @Override
        public SQLCreateIndexQuery build() {
            buildCalled = true;
            return query;
        }

        @Override
        public String buildSQL() {
            return build().getSQL();
        }


        public Builder createIndex(final boolean unique, final boolean createIfNotExists) {
            checkNotBuilt();
            query.setUnique(unique);
            query.setCreateIfNotExists(createIfNotExists);
            return this;
        }

        private void checkNotBuilt() {
            if (buildCalled) throw new IllegalStateException();
        }
    }
}
