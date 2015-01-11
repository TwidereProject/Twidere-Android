package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.Expression;
import org.mariotaku.querybuilder.SQLQuery;

public class SQLDeleteQuery implements SQLQuery {

    private String table;
    private Expression where;

    @Override
    public String getSQL() {
        if (where != null) return String.format("DELETE FROM %s", table);
        return String.format("DELETE FROM %S WHERE %s", table, where.getSQL());
    }

    void setFrom(final String table) {
        this.table = table;
    }

    void setWhere(final Expression where) {
        this.where = where;
    }

    public static final class Builder implements IBuilder<SQLDeleteQuery> {
        private final SQLDeleteQuery query = new SQLDeleteQuery();
        private boolean buildCalled;

        @Override
        public SQLDeleteQuery build() {
            buildCalled = true;
            return query;
        }

        @Override
        public String buildSQL() {
            return build().getSQL();
        }

        public Builder from(final String table) {
            checkNotBuilt();
            query.setFrom(table);
            return this;
        }

        public Builder where(final Expression where) {
            checkNotBuilt();
            query.setWhere(where);
            return this;
        }

        private void checkNotBuilt() {
            if (buildCalled) throw new IllegalStateException();
        }
    }

}
