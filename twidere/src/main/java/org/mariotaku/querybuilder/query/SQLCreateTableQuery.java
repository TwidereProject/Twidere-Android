package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.NewColumn;
import org.mariotaku.querybuilder.SQLLang;
import org.mariotaku.querybuilder.Utils;

public class SQLCreateTableQuery implements SQLLang {

	private boolean temporary;
	private boolean createIfNotExists;
	private String table;
	private NewColumn[] newColumns;
	private SQLSelectQuery selectStmt;

	SQLCreateTableQuery() {
	}

	@Override
	public String getSQL() {
		if (table == null) throw new NullPointerException("NAME must not be null!");
		if ((newColumns == null || newColumns.length == 0) && selectStmt == null)
			throw new NullPointerException("Columns or AS must not be null!");
		final StringBuilder sb = new StringBuilder("CREATE ");
		if (temporary) {
			sb.append("TEMPORARY ");
		}
		sb.append("TABLE ");
		if (createIfNotExists) {
			sb.append("IF NOT EXISTS ");
		}
		sb.append(String.format("%s ", table));
		if (newColumns != null && newColumns.length > 0) {
			sb.append(String.format("(%s)", Utils.toString(newColumns)));
		} else {
			sb.append(String.format("AS %s", selectStmt.getSQL()));
		}
		return sb.toString();
	}

	void setAs(final SQLSelectQuery selectStmt) {
		this.selectStmt = selectStmt;
	}

	void setCreateIfNotExists(final boolean createIfNotExists) {
		this.createIfNotExists = createIfNotExists;
	}

	void setNewColumns(final NewColumn[] newColumns) {
		this.newColumns = newColumns;
	}

	void setTable(final String table) {
		this.table = table;
	}

	void setTemporary(final boolean temporary) {
		this.temporary = temporary;
	}

	public static final class Builder implements IBuilder<SQLCreateTableQuery> {

		private final SQLCreateTableQuery query = new SQLCreateTableQuery();

		private boolean buildCalled;

		public Builder as(final SQLSelectQuery selectStmt) {
			checkNotBuilt();
			query.setAs(selectStmt);
			return this;
		}

		@Override
		public SQLCreateTableQuery build() {
			buildCalled = true;
			return query;
		}

		@Override
		public String buildSQL() {
			return build().getSQL();
		}

		public Builder columns(final NewColumn... newColumns) {
			checkNotBuilt();
			query.setNewColumns(newColumns);
			return this;
		}

		public Builder createTable(final boolean temporary, final boolean createIfNotExists, final String table) {
			checkNotBuilt();
			query.setTemporary(temporary);
			query.setCreateIfNotExists(createIfNotExists);
			query.setTable(table);
			return this;
		}

		public Builder createTable(final boolean createIfNotExists, final String table) {
			return createTable(false, createIfNotExists, table);
		}

		public Builder createTemporaryTable(final boolean createIfNotExists, final String table) {
			return createTable(true, createIfNotExists, table);
		}

		private void checkNotBuilt() {
			if (buildCalled) throw new IllegalStateException();
		}

	}

}
