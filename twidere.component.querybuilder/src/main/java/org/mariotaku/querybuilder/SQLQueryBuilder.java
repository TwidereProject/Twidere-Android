/**
 * This is free and unencumbered software released into the public domain.
 * <p/>
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p/>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * For more information, please refer to <http://unlicense.org/>
 */

package org.mariotaku.querybuilder;

import org.mariotaku.querybuilder.query.SQLAlterTableQuery;
import org.mariotaku.querybuilder.query.SQLCreateIndexQuery;
import org.mariotaku.querybuilder.query.SQLCreateTableQuery;
import org.mariotaku.querybuilder.query.SQLCreateTriggerQuery;
import org.mariotaku.querybuilder.query.SQLCreateViewQuery;
import org.mariotaku.querybuilder.query.SQLDeleteQuery;
import org.mariotaku.querybuilder.query.SQLDropTableQuery;
import org.mariotaku.querybuilder.query.SQLDropTriggerQuery;
import org.mariotaku.querybuilder.query.SQLDropViewQuery;
import org.mariotaku.querybuilder.query.SQLInsertQuery;
import org.mariotaku.querybuilder.query.SQLSelectQuery;
import org.mariotaku.querybuilder.query.SQLUpdateQuery;

public class SQLQueryBuilder {

    private SQLQueryBuilder() {
        throw new AssertionError("You can't create instance for this class");
    }

    public static SQLAlterTableQuery.Builder alterTable(final String table) {
        return new SQLAlterTableQuery.Builder().alterTable(table);
    }

    public static SQLCreateTableQuery.Builder createTable(final boolean temporary, final boolean createIfNotExists,
                                                          final String name) {
        return new SQLCreateTableQuery.Builder().createTable(temporary, createIfNotExists, name);
    }

    public static SQLCreateTableQuery.Builder createTable(final boolean createIfNotExists, final String name) {
        return createTable(false, createIfNotExists, name);
    }

    public static SQLCreateTableQuery.Builder createTable(final String name) {
        return createTable(false, false, name);
    }

    public static SQLCreateViewQuery.Builder createView(final boolean temporary, final boolean createIfNotExists,
                                                        final String name) {
        return new SQLCreateViewQuery.Builder().createView(temporary, createIfNotExists, name);
    }

    public static SQLCreateIndexQuery.Builder createIndex(final boolean unique, final boolean createIfNotExists) {
        return new SQLCreateIndexQuery.Builder().createIndex(unique, createIfNotExists);
    }


    public static SQLCreateTriggerQuery.Builder createTrigger(final boolean temporary, final boolean createIfNotExists,
                                                              final String name) {
        return new SQLCreateTriggerQuery.Builder().createTrigger(temporary, createIfNotExists, name);
    }

    public static SQLCreateViewQuery.Builder createView(final boolean createIfNotExists, final String name) {
        return createView(false, createIfNotExists, name);
    }

    public static SQLCreateViewQuery.Builder createView(final String name) {
        return createView(false, false, name);
    }

    public static SQLDeleteQuery.Builder deleteFrom(Table table) {
        return new SQLDeleteQuery.Builder().from(table);
    }

    public static SQLDropTableQuery dropTable(final boolean dropIfExists, final String table) {
        return new SQLDropTableQuery(dropIfExists, table);
    }

    public static SQLDropViewQuery dropView(final boolean dropIfExists, final String table) {
        return new SQLDropViewQuery(dropIfExists, table);
    }

    public static SQLDropTriggerQuery dropTrigger(final boolean dropIfExists, final String table) {
        return new SQLDropTriggerQuery(dropIfExists, table);
    }

    public static SQLInsertQuery.Builder insertInto(final OnConflict onConflict, final String table) {
        return new SQLInsertQuery.Builder().insertInto(onConflict, table);
    }

    public static SQLUpdateQuery.Builder update(final OnConflict onConflict, final Table table) {
        return new SQLUpdateQuery.Builder().update(onConflict, table);
    }

    public static SQLUpdateQuery.Builder update(final OnConflict onConflict, final String table) {
        return update(onConflict, new Table(table));
    }

    public static SQLInsertQuery.Builder insertInto(final String table) {
        return insertInto(null, table);
    }

    public static SQLSelectQuery.Builder select(final boolean distinct, final Selectable select) {
        return new SQLSelectQuery.Builder().select(distinct, select);
    }

    public static SQLSelectQuery.Builder select(final Selectable select) {
        return select(false, select);
    }
}
