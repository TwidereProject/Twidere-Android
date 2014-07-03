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

package org.mariotaku.querybuilder.query;

import org.mariotaku.querybuilder.OrderBy;
import org.mariotaku.querybuilder.SQLLang;
import org.mariotaku.querybuilder.SQLQueryException;
import org.mariotaku.querybuilder.Selectable;
import org.mariotaku.querybuilder.Where;

import java.util.ArrayList;
import java.util.List;

public class SQLSelectQuery implements Selectable {

	private final List<InternalQuery> internalQueries = new ArrayList<InternalQuery>();

	private InternalQuery currentInternalQuery;
	private OrderBy orderBy;
	private Integer limit = null, offset = null;

	SQLSelectQuery() {
		initCurrentQuery();
	}

	@Override
	public String getSQL() {
		final StringBuilder sb = new StringBuilder();
		final int size = internalQueries.size();
		for (int i = 0; i < size; i++) {
			if (i != 0) {
				sb.append("UNION ");
			}
			final InternalQuery query = internalQueries.get(i);
			sb.append(query.getSQL());

		}
		if (orderBy != null) {
			sb.append(String.format("ORDER BY %s ", orderBy.getSQL()));
		}
		if (limit != null) {
			sb.append(String.format("LIMIT %s ", limit));
			if (offset != null) {
				sb.append(String.format("OFFSET %s ", offset));
			}
		}
		return sb.toString();
	}

	private void initCurrentQuery() {
		currentInternalQuery = new InternalQuery();
		internalQueries.add(currentInternalQuery);
	}

	void setDistinct(final boolean distinct) {
		currentInternalQuery.setDistinct(distinct);
	}

	void setFrom(final Selectable from) {
		currentInternalQuery.setFrom(from);
	}

	void setGroupBy(final Selectable groupBy) {
		currentInternalQuery.setGroupBy(groupBy);
	}

	void setHaving(final Where having) {
		currentInternalQuery.setHaving(having);
	}

	void setLimit(final int limit) {
		this.limit = limit;
	}

	void setOffset(final int offset) {
		this.offset = offset;
	}

	void setOrderBy(final OrderBy orderBy) {
		this.orderBy = orderBy;
	}

	void setSelect(final Selectable select) {
		currentInternalQuery.setSelect(select);
	}

	void setWhere(final Where where) {
		currentInternalQuery.setWhere(where);
	}

	void union() {
		initCurrentQuery();
	}

	public static final class Builder implements IBuilder<SQLSelectQuery> {
		private boolean buildCalled;
		private final SQLSelectQuery query = new SQLSelectQuery();

		@Override
		public SQLSelectQuery build() {
			buildCalled = true;
			return query;
		}

		@Override
		public String buildSQL() {
			return build().getSQL();
		}

		public Builder from(final Selectable from) {
			checkNotBuilt();
			query.setFrom(from);
			return this;
		}

		public Builder groupBy(final Selectable groupBy) {
			checkNotBuilt();
			query.setGroupBy(groupBy);
			return this;
		}

		public Builder having(final Where having) {
			checkNotBuilt();
			query.setHaving(having);
			return this;
		}

		public Builder limit(final int limit) {
			checkNotBuilt();
			query.setLimit(limit);
			return this;
		}

		public Builder offset(final int offset) {
			query.setOffset(offset);
			return this;
		}

		public Builder orderBy(final OrderBy orderBy) {
			checkNotBuilt();
			query.setOrderBy(orderBy);
			return this;
		}

		public Builder select(final boolean distinct, final Selectable select) {
			checkNotBuilt();
			query.setSelect(select);
			query.setDistinct(distinct);
			return this;
		}

		public Builder select(final Selectable select) {
			checkNotBuilt();
			select(false, select);
			return this;
		}

		public Builder union() {
			checkNotBuilt();
			query.union();
			return this;
		}

		public Builder where(final Where where) {
			checkNotBuilt();
			query.setWhere(where);
			return this;
		}

		private void checkNotBuilt() {
			if (buildCalled) throw new IllegalStateException();
		}

	}

	private static class InternalQuery implements SQLLang {

		private boolean distinct;
		private Selectable select, from, groupBy;
		private Where where, having;

		@Override
		public String getSQL() {
			if (select == null) throw new SQLQueryException("selectable is null");
			final StringBuilder sb = new StringBuilder("SELECT ");
			if (distinct) {
				sb.append("DISTINCT ");
			}
			sb.append(String.format("%s ", select.getSQL()));
			if (!(select instanceof SQLSelectQuery) && from == null)
				throw new SQLQueryException("FROM not specified");
			else if (from != null) {
				if (from instanceof SQLSelectQuery) {
					sb.append(String.format("FROM (%s) ", from.getSQL()));
				} else {
					sb.append(String.format("FROM %s ", from.getSQL()));
				}
			}
			if (where != null) {
				sb.append(String.format("WHERE %s ", where.getSQL()));
			}
			if (groupBy != null) {
				sb.append(String.format("GROUP BY %s ", groupBy.getSQL()));
				if (having != null) {
					sb.append(String.format("HAVING %s ", having.getSQL()));
				}
			}
			return sb.toString();
		}

		void setDistinct(final boolean distinct) {
			this.distinct = distinct;
		}

		void setFrom(final Selectable from) {
			this.from = from;
		}

		void setGroupBy(final Selectable groupBy) {
			this.groupBy = groupBy;
		}

		void setHaving(final Where having) {
			this.having = having;
		}

		void setSelect(final Selectable select) {
			this.select = select;
		}

		void setWhere(final Where where) {
			this.where = where;
		}
	}
}
