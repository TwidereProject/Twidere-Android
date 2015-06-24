package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.OnConflict;
import org.mariotaku.querybuilder.SQLQuery;
import org.mariotaku.querybuilder.Utils;

public class SQLInsertQuery implements SQLQuery {

    private OnConflict onConflict;
    private String table;
    private String[] columns;
    private String values;

    SQLInsertQuery() {

    }

    @Override
    public String getSQL() {
        if (table == null) throw new NullPointerException("table must not be null!");
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT ");
        if (onConflict != null) {
            sb.append("OR ");
            sb.append(onConflict.getAction());
            sb.append(" ");
        }
        sb.append("INTO ");
        sb.append(table);
        sb.append(" (");
        sb.append(Utils.toString(columns, ',', false));
        sb.append(") ");
        sb.append("VALUES (");
        sb.append(values);
        sb.append(") ");
        return sb.toString();
    }

    void setColumns(final String[] columns) {
        this.columns = columns;
    }

    void setOnConflict(final OnConflict onConflict) {
        this.onConflict = onConflict;
    }

    void setSelect(final SQLSelectQuery select) {
        this.values = select.getSQL();
    }

    void setValues(final String... values) {
        this.values = Utils.toString(values, ',', false);
    }

    void setTable(final String table) {
        this.table = table;
    }

    public static final class Builder implements IBuilder<SQLInsertQuery> {

        private final SQLInsertQuery query = new SQLInsertQuery();

        private boolean buildCalled;

        @Override
        public SQLInsertQuery build() {
            buildCalled = true;
            return query;
        }

        @Override
        public String buildSQL() {
            return build().getSQL();
        }

        public Builder columns(final String[] columns) {
            checkNotBuilt();
            query.setColumns(columns);
            return this;
        }

        public Builder values(final String[] values) {
            checkNotBuilt();
            query.setValues(values);
            return this;
        }

        public Builder values(final String values) {
            checkNotBuilt();
            query.setValues(values);
            return this;
        }

        public Builder insertInto(final OnConflict onConflict, final String table) {
            checkNotBuilt();
            query.setOnConflict(onConflict);
            query.setTable(table);
            return this;
        }

        public Builder insertInto(final String table) {
            return insertInto(null, table);
        }

        public Builder select(final SQLSelectQuery select) {
            checkNotBuilt();
            query.setSelect(select);
            return this;
        }

        private void checkNotBuilt() {
            if (buildCalled) throw new IllegalStateException();
        }

    }

}
