package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.NewColumn;
import org.mariotaku.querybuilder.SQLLang;

public class SQLAlterTableQuery implements SQLLang {

	private String table;
	private String renameTo;
	private NewColumn addColumn;

	@Override
	public String getSQL() {
		if (table == null) throw new NullPointerException("table must not be null!");
		if (renameTo == null && addColumn == null) throw new NullPointerException();
		if (renameTo != null) return String.format("ALTER TABLE %s RENAME TO %s", table, renameTo);
		return String.format("ALTER TABLE %s ADD COLUMN %s", table, addColumn.getSQL());
	}

	void setAddColumn(final NewColumn addColumn) {
		this.addColumn = addColumn;
	}

	void setRenameTo(final String renameTo) {
		this.renameTo = renameTo;
	}

	void setTable(final String table) {
		this.table = table;
	}

	public static final class Builder implements IBuilder<SQLAlterTableQuery> {

		private boolean buildCalled;

		private final SQLAlterTableQuery query = new SQLAlterTableQuery();

		public Builder addColumn(final NewColumn addColumn) {
			checkNotBuilt();
			query.setAddColumn(addColumn);
			return this;
		}

		public Builder alterTable(final String table) {
			checkNotBuilt();
			query.setTable(table);
			return this;
		}

		@Override
		public SQLAlterTableQuery build() {
			return query;
		}

		@Override
		public String buildSQL() {
			return build().getSQL();
		}

		public Builder renameTo(final String renameTo) {
			checkNotBuilt();
			query.setRenameTo(renameTo);
			return this;
		}

		private void checkNotBuilt() {
			if (buildCalled) throw new IllegalStateException();
		}
	}

}
