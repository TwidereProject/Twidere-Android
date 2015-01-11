package org.mariotaku.querybuilder;

public class SQLFunctions {

    public static String SUM(final String val) {
        return String.format("SUM (%s)", val);
    }

    public static String COUNT(final String val) {
        return String.format("COUNT (%s)", val);
    }

}
