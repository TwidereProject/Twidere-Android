package org.mariotaku.querybuilder.query;

public class SQLDropTableQuery extends SQLDropQuery {

    public SQLDropTableQuery(final boolean dropIfExists, final String table) {
        super(dropIfExists, "TABLE", table);
    }

}
