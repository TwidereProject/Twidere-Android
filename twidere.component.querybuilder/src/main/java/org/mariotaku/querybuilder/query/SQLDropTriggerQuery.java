package org.mariotaku.querybuilder.query;

public class SQLDropTriggerQuery extends SQLDropQuery {

    public SQLDropTriggerQuery(final boolean dropIfExists, final String table) {
        super(dropIfExists, "TRIGGER", table);
    }

}
