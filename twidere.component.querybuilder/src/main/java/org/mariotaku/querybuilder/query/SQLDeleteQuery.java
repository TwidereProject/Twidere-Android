package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.Expression;
import org.mariotaku.querybuilder.SQLQuery;
import org.mariotaku.querybuilder.Table;

public class SQLDeleteQuery implements SQLQuery {

    private Table table;
    private Expression where;

    @Override
    public String getSQL() {
        if (where == null) return String.format("DELETE FROM %s", table.getSQL());
        return String.format("DELETE FROM %S WHERE %s", table.getSQL(), where.getSQL());
    }

    void setFrom(final Table table) {
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

        public Builder from(final Table table) {
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
