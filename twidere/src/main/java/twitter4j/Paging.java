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
public final class Paging {
	private int page = -1;
	private int count = -1;
	private long sinceId = -1;
	private long maxId = -1;

	// since only
	static char[] S = new char[] { 's' };
	// since, max_id, count, page
	static char[] SMCP = new char[] { 's', 'm', 'c', 'p' };

	static final String COUNT = "count";
	// somewhat GET list statuses requires "per_page" instead of "count"
	// @see <a
	// href="https://dev.twitter.com/docs/api/1.1/get/:user/lists/:id/statuses">GET
	// :user/lists/:id/statuses | Twitter Developers</a>
	static final String PER_PAGE = "per_page";

	private static HttpParameter[] NULL_PARAMETER_ARRAY = new HttpParameter[0];

	private static List<HttpParameter> NULL_PARAMETER_LIST = new ArrayList<HttpParameter>(0);

	public Paging() {
	}

	public Paging(final int page) {
		setPage(page);
	}

	public Paging(final int page, final int count) {
		this(page);
		setCount(count);
	}

	public Paging(final int page, final int count, final long sinceId) {
		this(page, count);
		setSinceId(sinceId);
	}

	public Paging(final int page, final int count, final long sinceId, final long maxId) {
		this(page, count, sinceId);
		setMaxId(maxId);
	}

	public Paging(final int page, final long sinceId) {
		this(page);
		setSinceId(sinceId);
	}

	public Paging(final long sinceId) {
		setSinceId(sinceId);
	}

	public Paging count(final int count) {
		setCount(count);
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof Paging)) return false;

		final Paging paging = (Paging) o;

		if (count != paging.count) return false;
		if (maxId != paging.maxId) return false;
		if (page != paging.page) return false;
		if (sinceId != paging.sinceId) return false;

		return true;
	}

	public int getCount() {
		return count;
	}

	public long getMaxId() {
		return maxId;
	}

	public int getPage() {
		return page;
	}

	public long getSinceId() {
		return sinceId;
	}

	@Override
	public int hashCode() {
		int result = page;
		result = 31 * result + count;
		result = 31 * result + (int) (sinceId ^ sinceId >>> 32);
		result = 31 * result + (int) (maxId ^ maxId >>> 32);
		return result;
	}

	public Paging maxId(final long maxId) {
		setMaxId(maxId);
		return this;
	}

	public void setCount(final int count) {
		if (count < 1) throw new IllegalArgumentException("count should be positive integer. passed:" + count);
		this.count = count;
	}

	public void setMaxId(final long maxId) {
		if (maxId < 1) throw new IllegalArgumentException("max_id should be positive integer. passed:" + maxId);
		this.maxId = maxId;
	}

	public void setPage(final int page) {
		if (page < 1) throw new IllegalArgumentException("page should be positive integer. passed:" + page);
		this.page = page;
	}

	public void setSinceId(final long sinceId) {
		if (sinceId < 1) throw new IllegalArgumentException("since_id should be positive integer. passed:" + sinceId);
		this.sinceId = sinceId;
	}

	public Paging sinceId(final long sinceId) {
		setSinceId(sinceId);
		return this;
	}

	@Override
	public String toString() {
		return "Paging{" + "page=" + page + ", count=" + count + ", sinceId=" + sinceId + ", maxId=" + maxId + '}';
	}

	private void addPostParameter(final char[] supportedParams, final char paramKey,
			final List<HttpParameter> pagingParams, final String paramName, final long paramValue) {
		boolean supported = false;
		for (final char supportedParam : supportedParams) {
			if (supportedParam == paramKey) {
				supported = true;
				break;
			}
		}
		if (!supported && -1 != paramValue)
			throw new IllegalStateException("Paging parameter [" + paramName
					+ "] is not supported with this operation.");
		if (-1 != paramValue) {
			pagingParams.add(new HttpParameter(paramName, String.valueOf(paramValue)));
		}
	}

	/* package */HttpParameter[] asPostParameterArray() {
		final List<HttpParameter> list = asPostParameterList(SMCP, COUNT);
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
	/* package */HttpParameter[] asPostParameterArray(final char[] supportedParams, final String perPageParamName) {
		final java.util.List<HttpParameter> pagingParams = new ArrayList<HttpParameter>(supportedParams.length);
		addPostParameter(supportedParams, 's', pagingParams, "since_id", getSinceId());
		addPostParameter(supportedParams, 'm', pagingParams, "max_id", getMaxId());
		addPostParameter(supportedParams, 'c', pagingParams, perPageParamName, getCount());
		addPostParameter(supportedParams, 'p', pagingParams, "page", getPage());
		if (pagingParams.size() == 0)
			return NULL_PARAMETER_ARRAY;
		else
			return pagingParams.toArray(new HttpParameter[pagingParams.size()]);
	}

	/* package */List<HttpParameter> asPostParameterList() {
		return asPostParameterList(SMCP, COUNT);
	}

	/* package */List<HttpParameter> asPostParameterList(final char[] supportedParams) {
		return asPostParameterList(supportedParams, COUNT);
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
	/* package */List<HttpParameter> asPostParameterList(final char[] supportedParams, final String perPageParamName) {
		final java.util.List<HttpParameter> pagingParams = new ArrayList<HttpParameter>(supportedParams.length);
		addPostParameter(supportedParams, 's', pagingParams, "since_id", getSinceId());
		addPostParameter(supportedParams, 'm', pagingParams, "max_id", getMaxId());
		addPostParameter(supportedParams, 'c', pagingParams, perPageParamName, getCount());
		addPostParameter(supportedParams, 'p', pagingParams, "page", getPage());
		if (pagingParams.size() == 0)
			return NULL_PARAMETER_LIST;
		else
			return pagingParams;
	}
}
