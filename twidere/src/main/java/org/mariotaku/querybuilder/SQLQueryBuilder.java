/**
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
 */

package org.mariotaku.querybuilder;

import org.mariotaku.querybuilder.query.SQLAlterTableQuery;
import org.mariotaku.querybuilder.query.SQLCreateTableQuery;
import org.mariotaku.querybuilder.query.SQLCreateViewQuery;
import org.mariotaku.querybuilder.query.SQLDropTableQuery;
import org.mariotaku.querybuilder.query.SQLDropViewQuery;
import org.mariotaku.querybuilder.query.SQLInsertIntoQuery;
import org.mariotaku.querybuilder.query.SQLInsertIntoQuery.OnConflict;
import org.mariotaku.querybuilder.query.SQLSelectQuery;

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

	public static SQLCreateViewQuery.Builder createView(final boolean createIfNotExists, final String name) {
		return createView(false, createIfNotExists, name);
	}

	public static SQLCreateViewQuery.Builder createView(final String name) {
		return createView(false, false, name);
	}

	public static SQLDropTableQuery dropTable(final boolean dropIfExists, final String table) {
		return new SQLDropTableQuery(dropIfExists, table);
	}

	public static SQLDropViewQuery dropView(final boolean dropIfExists, final String table) {
		return new SQLDropViewQuery(dropIfExists, table);
	}

	public static SQLInsertIntoQuery.Builder insertInto(final OnConflict onConflict, final String table) {
		return new SQLInsertIntoQuery.Builder().insertInto(onConflict, table);
	}

	public static SQLInsertIntoQuery.Builder insertInto(final String table) {
		return insertInto(null, table);
	}

	public static SQLSelectQuery.Builder select(final boolean distinct, final Selectable select) {
		return new SQLSelectQuery.Builder().select(distinct, select);
	}

	public static SQLSelectQuery.Builder select(final Selectable select) {
		return select(false, select);
	}
}
