/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package twitter4j;

import java.util.ArrayList;
import java.util.List;

import twitter4j.http.HttpParameter;

/**
 * Controls pagination.<br>
 * It is possible to use the same Paging instance in a multi-threaded context
 * only if the instance is treated immutably.<br>
 * But basically instance of this class is NOT thread safe. A client should
 * instantiate Paging class per thread.<br>
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class CursorPaging {
	private int count = -1;
	private long cursor = -1;

	static final String COUNT = "count";
	static final String CURSOR = "cursor";

	private static HttpParameter[] NULL_PARAMETER_ARRAY = new HttpParameter[0];
	private static List<HttpParameter> NULL_PARAMETER_LIST = new ArrayList<HttpParameter>(0);

	public CursorPaging() {
	}

	public CursorPaging(final int count) {
		setCount(count);
	}

	public CursorPaging(final long cursor) {
		setCursor(cursor);
	}

	public CursorPaging(final long cursor, final int count) {
		setCursor(cursor);
		setCount(count);
	}

	public CursorPaging count(final int count) {
		setCount(count);
		return this;
	}

	public CursorPaging cursor(final long cursor) {
		setCursor(cursor);
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof CursorPaging)) return false;
		final CursorPaging paging = (CursorPaging) o;
		if (count != paging.count) return false;
		if (cursor != paging.cursor) return false;
		return true;
	}

	public int getCount() {
		return count;
	}

	public long getCursor() {
		return cursor;
	}

	@Override
	public int hashCode() {
		int result = count;
		result = 31 * result + (int) (cursor ^ cursor >>> 32);
		return result;
	}

	public void setCount(final int count) {
		if (count < 1) throw new IllegalArgumentException("count should be positive integer. passed:" + count);
		this.count = count;
	}

	public void setCursor(final long cursor) {
		if (cursor < 1 && cursor != -1)
			throw new IllegalArgumentException("cursor should be -1 or positive integer. passed:" + cursor);
		this.cursor = cursor;
	}

	@Override
	public String toString() {
		return "Paging{" + "cursor=" + cursor + ", count=" + count + '}';
	}

	/* package */HttpParameter[] asPostParameterArray() {
		final List<HttpParameter> list = asPostParameterList();
		if (list.size() == 0) return NULL_PARAMETER_ARRAY;
		return list.toArray(new HttpParameter[list.size()]);
	}

	/**
	 * Converts the pagination parameters into a List of PostParameter.<br>
	 * This method also Validates the preset parameters, and throws
	 * IllegalStateException if any unsupported parameter is set.
	 * 
	 * @return list of PostParameter
	 */
	/* package */List<HttpParameter> asPostParameterList() {
		final List<HttpParameter> pagingParams = new ArrayList<HttpParameter>();
		if (cursor > 0 || cursor == -1) {
			pagingParams.add(new HttpParameter(CURSOR, cursor));
		}
		if (count > 0) {
			pagingParams.add(new HttpParameter(COUNT, count));
		}
		if (pagingParams.size() == 0)
			return NULL_PARAMETER_LIST;
		else
			return pagingParams;
	}
}
