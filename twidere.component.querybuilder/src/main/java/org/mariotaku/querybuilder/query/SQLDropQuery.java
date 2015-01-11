package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.SQLQuery;

public class SQLDropQuery implements SQLQuery {

    private final boolean dropIfExists;
    private final String type;
    private final String target;

    public SQLDropQuery(final boolean dropIfExists, final String type, final String target) {
        if (target == null) throw new NullPointerException();
        this.dropIfExists = dropIfExists;
        this.type = type;
        this.target = target;
    }

    @Override
    public final String getSQL() {
        if (dropIfExists) return String.format("DROP %s IF EXISTS %s", type, target);
        return String.format("DROP %s %s", type, target);
    }

}
