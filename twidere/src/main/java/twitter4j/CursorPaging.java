/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

import twitter4j.http.HttpParameter;

import java.util.ArrayList;
import java.util.List;

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
	 * @param supportedParams char array representation of supported parameters
	 * @param perPageParamName name used for per-page parameter.
	 *            getUserListStatuses() requires "per_page" instead of "count".
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
