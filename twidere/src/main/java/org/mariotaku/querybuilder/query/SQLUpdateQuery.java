package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.Expression;
import org.mariotaku.querybuilder.OnConflict;
import org.mariotaku.querybuilder.SQLQuery;
import org.mariotaku.querybuilder.SetValue;
import org.mariotaku.querybuilder.Utils;

import java.util.Locale;

public class SQLUpdateQuery implements SQLQuery {

    private OnConflict onConflict;
    private String table;
    private SetValue[] values;
    private Expression where;

    SQLUpdateQuery() {

    }

    @Override
    public String getSQL() {
        if (table == null) throw new NullPointerException("table must not be null!");
        final StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        if (onConflict != null) {
            sb.append(String.format(Locale.ROOT, "OR %s ", onConflict.getAction()));
        }
        sb.append(String.format(Locale.ROOT, "%s ", table));
        sb.append(String.format(Locale.ROOT, "SET %s ", Utils.toString(values, ',', false)));
        if (where != null) {
            sb.append(String.format(Locale.ROOT, "WHERE %s ", where.getSQL()));
        }
        return sb.toString();
    }

    void setWhere(final Expression where) {
        this.where = where;
    }

    void setValues(final SetValue[] columns) {
        this.values = columns;
    }

    void setOnConflict(final OnConflict onConflict) {
        this.onConflict = onConflict;
    }

    void setTable(final String table) {
        this.table = table;
    }

    public static final class Builder implements IBuilder<SQLUpdateQuery> {

        private final SQLUpdateQuery query = new SQLUpdateQuery();

        private boolean buildCalled;

        @Override
        public SQLUpdateQuery build() {
            buildCalled = true;
            return query;
        }

        @Override
        public String buildSQL() {
            return build().getSQL();
        }

        public Builder set(final SetValue... values) {
            checkNotBuilt();
            query.setValues(values);
            return this;
        }

        public Builder where(final Expression where) {
            checkNotBuilt();
            query.setWhere(where);
            return this;
        }

        public Builder update(final OnConflict onConflict, final String table) {
            checkNotBuilt();
            query.setOnConflict(onConflict);
            query.setTable(table);
            return this;
        }

        public Builder update(final String table) {
            return update(null, table);
        }

        private void checkNotBuilt() {
            if (buildCalled) throw new IllegalStateException();
        }

    }

}
