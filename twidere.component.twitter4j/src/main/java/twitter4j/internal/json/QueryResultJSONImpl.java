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

package twitter4j.internal.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.http.HttpResponse;

import static twitter4j.internal.util.InternalParseUtil.getDouble;
import static twitter4j.internal.util.InternalParseUtil.getHTMLUnescapedString;
import static twitter4j.internal.util.InternalParseUtil.getInt;
import static twitter4j.internal.util.InternalParseUtil.getLong;
import static twitter4j.internal.util.InternalParseUtil.getRawString;
import static twitter4j.internal.util.InternalParseUtil.getURLDecodedString;

/**
 * A data class representing search API response
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
/* package */final class QueryResultJSONImpl implements QueryResult {

	private long sinceId;
	private long maxId;
	private String refreshUrl;
	private int resultsPerPage;
	private String warning;
	private double completedIn;
	private int page;
	private String query;
	private Status[] statuses;

	/* package */QueryResultJSONImpl(final HttpResponse res) throws TwitterException {
		final JSONObject json = res.asJSONObject();
		try {
			final JSONObject search_metadata = json.getJSONObject("search_metadata");
			sinceId = getLong("since_id", search_metadata);
			maxId = getLong("max_id", search_metadata);
			refreshUrl = getHTMLUnescapedString("refresh_url", search_metadata);

			resultsPerPage = getInt("results_per_page", search_metadata);
			warning = getRawString("warning", search_metadata);
			completedIn = getDouble("completed_in", search_metadata);
			page = getInt("page", search_metadata);
			query = getURLDecodedString("query", search_metadata);
			final JSONArray array = json.getJSONArray("statuses");
			final int statuses_length = array.length();
			statuses = new Status[statuses_length];
			for (int i = 0; i < statuses_length; i++) {
				final JSONObject tweet = array.getJSONObject(i);
				statuses[i] = new StatusJSONImpl(tweet);
			}
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone.getMessage() + ":" + json.toString(), jsone);
		}
	}

	/* package */QueryResultJSONImpl(final Query query) {
		super();
		sinceId = query.getSinceId();
		resultsPerPage = query.getRpp();
		page = query.getPage();
		statuses = new Status[0];
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final QueryResult that = (QueryResult) o;

		if (Double.compare(that.getCompletedIn(), completedIn) != 0) return false;
		if (maxId != that.getMaxId()) return false;
		if (page != that.getPage()) return false;
		if (resultsPerPage != that.getResultsPerPage()) return false;
		if (sinceId != that.getSinceId()) return false;
		if (!query.equals(that.getQuery())) return false;
		if (refreshUrl != null ? !refreshUrl.equals(that.getRefreshUrl()) : that.getRefreshUrl() != null) return false;
		if (statuses != null ? !Arrays.equals(statuses, that.getStatuses()) : that.getStatuses() != null) return false;
		if (warning != null ? !warning.equals(that.getWarning()) : that.getWarning() != null) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getCompletedIn() {
		return completedIn;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getMaxId() {
		return maxId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPage() {
		return page;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getQuery() {
		return query;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRefreshUrl() {
		return refreshUrl;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getResultsPerPage() {
		return resultsPerPage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getSinceId() {
		return sinceId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Status[] getStatuses() {
		return statuses;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getWarning() {
		return warning;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = (int) (sinceId ^ sinceId >>> 32);
		result = 31 * result + (int) (maxId ^ maxId >>> 32);
		result = 31 * result + (refreshUrl != null ? refreshUrl.hashCode() : 0);
		result = 31 * result + resultsPerPage;
		result = 31 * result + (warning != null ? warning.hashCode() : 0);
		temp = completedIn != +0.0d ? Double.doubleToLongBits(completedIn) : 0L;
		result = 31 * result + (int) (temp ^ temp >>> 32);
		result = 31 * result + page;
		result = 31 * result + query.hashCode();
		result = 31 * result + (statuses != null ? Arrays.hashCode(statuses) : 0);
		return result;
	}

    @Override
    public String toString() {
        return "QueryResultJSONImpl{" +
                "sinceId=" + sinceId +
                ", maxId=" + maxId +
                ", refreshUrl='" + refreshUrl + '\'' +
                ", resultsPerPage=" + resultsPerPage +
                ", warning='" + warning + '\'' +
                ", completedIn=" + completedIn +
                ", page=" + page +
                ", query='" + query + '\'' +
                ", statuses=" + Arrays.toString(statuses) +
                '}';
    }
}
