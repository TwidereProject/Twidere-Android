package org.mariotaku.querybuilder;

/**
 * Created by mariotaku on 14-8-7.
 */
public enum OnConflict {
    ROLLBACK("ROLLBACK"), ABORT("ABORT"), REPLACE("REPLACE"), FAIL("FAIL"), IGNORE("IGNORE");
    private final String action;

    OnConflict(final String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
