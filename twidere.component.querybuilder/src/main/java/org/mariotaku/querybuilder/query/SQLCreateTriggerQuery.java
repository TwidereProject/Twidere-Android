package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.Columns;
import org.mariotaku.querybuilder.Expression;
import org.mariotaku.querybuilder.SQLLang;
import org.mariotaku.querybuilder.SQLQuery;
import org.mariotaku.querybuilder.Table;
import org.mariotaku.querybuilder.Utils;

import java.util.Locale;

/**
 * Created by mariotaku on 14-8-6.
 */
public class SQLCreateTriggerQuery implements SQLQuery {

    private boolean temporary;
    private boolean createIfNotExists;
    private boolean forEachRow;
    private String name;
    private Table on;
    private Type type;
    private Event event;
    private Columns updateOf;
    private SQLQuery[] actions;
    private Expression when;

    void setActions(SQLQuery[] actions) {
        this.actions = actions;
    }

    void setForEachRow(boolean forEachRow) {
        this.forEachRow = forEachRow;
    }

    void setOn(Table on) {
        this.on = on;
    }

    void setUpdateOf(Columns updateOf) {
        this.updateOf = updateOf;
    }

    void setType(Type type) {
        this.type = type;
    }

    void setEvent(Event event) {
        this.event = event;
    }

    void setWhen(Expression when) {
        this.when = when;
    }

    @Override
    public String getSQL() {
        if (name == null) throw new NullPointerException("NAME must not be null!");
        if (event == null) throw new NullPointerException("EVENT must not be null!");
        if (on == null) throw new NullPointerException("ON must not be null!");
        if (actions == null) throw new NullPointerException("ACTIONS must not be null!");
        final StringBuilder sb = new StringBuilder("CREATE ");
        if (temporary) {
            sb.append("TEMPORARY ");
        }
        sb.append("TRIGGER ");
        if (createIfNotExists) {
            sb.append("IF NOT EXISTS ");
        }
        sb.append(name);
        sb.append(' ');
        if (type != null) {
            sb.append(type.getSQL());
            sb.append(' ');
        }
        sb.append(event.getSQL());
        sb.append(' ');
        if (event == Event.UPDATE) {
            sb.append(String.format(Locale.ROOT, "%s ", updateOf.getSQL()));
        }
        sb.append(String.format(Locale.ROOT, "ON %s ", on.getSQL()));
        if (forEachRow) {
            sb.append("FOR EACH ROW ");
        }
        if (when != null) {
            sb.append(String.format(Locale.ROOT, "WHEN %s ", when.getSQL()));
        }
        sb.append(String.format(Locale.ROOT, "BEGIN %s; END", Utils.toString(actions, ';', true)));
        return sb.toString();
    }

    void setCreateIfNotExists(final boolean createIfNotExists) {
        this.createIfNotExists = createIfNotExists;
    }

    void setName(final String name) {
        this.name = name;
    }

    void setTemporary(final boolean temporary) {
        this.temporary = temporary;
    }

    public static enum Type implements SQLLang {
        BEFORE("BEFORE"), AFTER("AFTER"), INSTEAD_OF("INSTEAD OF");
        private final String lang;

        Type(String lang) {
            this.lang = lang;
        }

        @Override
        public String getSQL() {
            return lang;
        }
    }

    public static enum Event implements SQLLang {
        INSERT("INSERT"), DELETE("DELETE"), UPDATE("UPDATE");
        private final String lang;

        Event(String lang) {
            this.lang = lang;
        }

        @Override
        public String getSQL() {
            return lang;
        }
    }

    public static class Builder implements IBuilder<SQLCreateTriggerQuery> {

        private final SQLCreateTriggerQuery query = new SQLCreateTriggerQuery();
        private boolean buildCalled;

        public Builder forEachRow(final boolean forEachRow) {
            checkNotBuilt();
            query.setForEachRow(forEachRow);
            return this;
        }

        public Builder on(final Table on) {
            checkNotBuilt();
            query.setOn(on);
            return this;
        }

        public Builder event(Event event) {
            checkNotBuilt();
            query.setEvent(event);
            return this;
        }

        public Builder type(Type type) {
            checkNotBuilt();
            query.setType(type);
            return this;
        }

        public Builder updateOf(Columns updateOf) {
            checkNotBuilt();
            query.setUpdateOf(updateOf);
            return this;
        }

        public Builder actions(SQLQuery... actions) {
            checkNotBuilt();
            query.setActions(actions);
            return this;
        }

        public Builder when(Expression when) {
            checkNotBuilt();
            query.setWhen(when);
            return this;
        }

        @Override
        public SQLCreateTriggerQuery build() {
            buildCalled = true;
            return query;
        }

        @Override
        public String buildSQL() {
            return build().getSQL();
        }

        private void checkNotBuilt() {
            if (buildCalled) throw new IllegalStateException();
        }

        public Builder createTrigger(boolean temporary, boolean createIfNotExists, String name) {
            checkNotBuilt();
            query.setTemporary(temporary);
            query.setCreateIfNotExists(createIfNotExists);
            query.setName(name);
            return this;
        }
    }
}
