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

import java.util.ArrayList;
import java.util.Arrays;

import twitter4j.http.HttpParameter;
import twitter4j.internal.util.InternalStringUtil;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
public final class FilterQuery implements java.io.Serializable {
	private static final long serialVersionUID = 430966623248982833L;
	private int count;
	private long[] follow;
	private String[] track;
	private double[][] locations;

	/**
	 * Creates a new FilterQuery
	 */
	public FilterQuery() {
		count = 0;
		follow = null;
		track = null;
		locations = null;
	}

	/**
	 * Creates a new FilterQuery
	 * 
	 * @param count Indicates the number of previous statuses to stream before
	 *            transitioning to the live stream.
	 * @param follow Specifies the users, by ID, to receive public tweets from.
	 */
	public FilterQuery(final int count, final long[] follow) {
		this();
		this.count = count;
		this.follow = follow;
	}

	/**
	 * Creates a new FilterQuery
	 * 
	 * @param count Indicates the number of previous statuses to stream before
	 *            transitioning to the live stream.
	 * @param follow Specifies the users, by ID, to receive public tweets from.
	 * @param track Specifies keywords to track.
	 */
	public FilterQuery(final int count, final long[] follow, final String[] track) {
		this();
		this.count = count;
		this.follow = follow;
		this.track = track;
	}

	/**
	 * Creates a new FilterQuery
	 * 
	 * @param count Indicates the number of previous statuses to stream before
	 *            transitioning to the live stream.
	 * @param follow Specifies the users, by ID, to receive public tweets from.
	 * @param track Specifies keywords to track.
	 * @param locations Specifies the locations to track. 2D array
	 */
	public FilterQuery(final int count, final long[] follow, final String[] track, final double[][] locations) {
		this.count = count;
		this.follow = follow;
		this.track = track;
		this.locations = locations;
	}

	/**
	 * Creates a new FilterQuery
	 * 
	 * @param follow Specifies the users, by ID, to receive public tweets from.
	 */
	public FilterQuery(final long[] follow) {
		this();
		count = 0;
		this.follow = follow;
	}

	/**
	 * Sets count
	 * 
	 * @param count Indicates the number of previous statuses to stream before
	 *            transitioning to the live stream.
	 * @return this instance
	 */
	public FilterQuery count(final int count) {
		this.count = count;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final FilterQuery that = (FilterQuery) o;

		if (count != that.count) return false;
		if (!Arrays.equals(follow, that.follow)) return false;
		if (!Arrays.equals(track, that.track)) return false;

		return true;
	}

	/**
	 * Sets follow
	 * 
	 * @param follow Specifies the users, by ID, to receive public tweets from.
	 * @return this instance
	 */
	public FilterQuery follow(final long[] follow) {
		this.follow = follow;
		return this;
	}

	@Override
	public int hashCode() {
		int result = count;
		result = 31 * result + (follow != null ? Arrays.hashCode(follow) : 0);
		result = 31 * result + (track != null ? Arrays.hashCode(track) : 0);
		return result;
	}

	/**
	 * Sets locations
	 * 
	 * @param locations Specifies the locations to track. 2D array
	 * @return this instance
	 */
	public FilterQuery locations(final double[][] locations) {
		this.locations = locations;
		return this;
	}

	@Override
	public String toString() {
		return "FilterQuery{" + "count=" + count + ", follow=" + Arrays.toString(follow) + ", track="
				+ (track == null ? null : Arrays.asList(track)) + ", locations="
				+ (locations == null ? null : Arrays.asList(locations)) + '}';
	}

	/**
	 * Sets track
	 * 
	 * @param track Specifies keywords to track.
	 * @return this instance
	 */
	public FilterQuery track(final String[] track) {
		this.track = track;
		return this;
	}

	private String toLocationsString(final double[][] keywords) {
		final StringBuilder buf = new StringBuilder(20 * keywords.length * 2);
		for (final double[] keyword : keywords) {
			if (0 != buf.length()) {
				buf.append(",");
			}
			buf.append(keyword[0]);
			buf.append(",");
			buf.append(keyword[1]);
		}
		return buf.toString();
	}

	/* package */HttpParameter[] asHttpParameterArray(final HttpParameter stallWarningsParam) {
		final ArrayList<HttpParameter> params = new ArrayList<HttpParameter>();

		params.add(new HttpParameter("count", count));
		if (follow != null && follow.length > 0) {
			params.add(new HttpParameter("follow", InternalStringUtil.join(follow)));
		}
		if (track != null && track.length > 0) {
			params.add(new HttpParameter("track", InternalStringUtil.join(track)));
		}
		if (locations != null && locations.length > 0) {
			params.add(new HttpParameter("locations", toLocationsString(locations)));
		}
		params.add(stallWarningsParam);
		final HttpParameter[] paramArray = new HttpParameter[params.size()];
		return params.toArray(paramArray);
	}
}
