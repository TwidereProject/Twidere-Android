package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.SQLLang;

public class SQLCreateViewQuery implements SQLLang {

	private boolean temporary;
	private boolean createIfNotExists;
	private String name;
	private SQLSelectQuery selectStmt;

	SQLCreateViewQuery() {

	}

	@Override
	public String getSQL() {
		if (name == null) throw new NullPointerException("NAME must not be null!");
		if (selectStmt == null) throw new NullPointerException("SELECT statement must not be null!");
		final StringBuilder sb = new StringBuilder("CREATE ");
		if (temporary) {
			sb.append("TEMPORARY ");
		}
		sb.append("VIEW ");
		if (createIfNotExists) {
			sb.append("IF NOT EXISTS ");
		}
		sb.append(String.format("%s AS %s", name, selectStmt.getSQL()));
		return sb.toString();
	}

	void setAs(final SQLSelectQuery selectStmt) {
		this.selectStmt = selectStmt;
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

	public static final class Builder implements IBuilder<SQLCreateViewQuery> {

		private boolean buildCalled;

		private final SQLCreateViewQuery query = new SQLCreateViewQuery();

		public Builder as(final SQLSelectQuery selectStmt) {
			checkNotBuilt();
			query.setAs(selectStmt);
			return this;
		}

		@Override
		public SQLCreateViewQuery build() {
			return query;
		}

		@Override
		public String buildSQL() {
			return build().getSQL();
		}

		public Builder createTemporaryView(final boolean createIfNotExists, final String name) {
			return createView(true, createIfNotExists, name);
		}

		public Builder createView(final boolean temporary, final boolean createIfNotExists, final String name) {
			checkNotBuilt();
			query.setTemporary(temporary);
			query.setCreateIfNotExists(createIfNotExists);
			query.setName(name);
			return this;
		}

		public Builder createView(final boolean createIfNotExists, final String name) {
			return createView(false, createIfNotExists, name);
		}

		private void checkNotBuilt() {
			if (buildCalled) throw new IllegalStateException();
		}
	}
}
