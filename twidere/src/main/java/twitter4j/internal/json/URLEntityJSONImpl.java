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

package twitter4j.internal.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.TwitterException;
import twitter4j.URLEntity;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A data class representing one single URL entity.
 * 
 * @author Mocel - mocel at guma.jp
 * @since Twitter4J 2.1.9
 */
/* package */final class URLEntityJSONImpl implements URLEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1326410198426703277L;
	private int start = -1;
	private int end = -1;
	private URL url;
	private URL expandedURL;
	private String displayURL;

	/* For serialization purposes only. */
	/* package */URLEntityJSONImpl() {

	}

	/* package */URLEntityJSONImpl(final int start, final int end, final String url, final String expandedURL,
			final String displayURL) {
		super();
		this.start = start;
		this.end = end;
		try {
			this.url = new URL(url);
		} catch (final MalformedURLException e) {
			try {
				this.url = new URL("http://example.com/");
			} catch (final MalformedURLException ignore) {
			}
		}
		try {
			this.expandedURL = new URL(expandedURL);
		} catch (final MalformedURLException e) {
			try {
				this.expandedURL = new URL("http://example.com/");
			} catch (final MalformedURLException ignore) {
			}
		}
		this.displayURL = displayURL;
	}

	/* package */URLEntityJSONImpl(final JSONObject json) throws TwitterException {
		super();
		init(json);
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final URLEntityJSONImpl that = (URLEntityJSONImpl) o;

		if (end != that.end) return false;
		if (start != that.start) return false;
		if (displayURL != null ? !displayURL.equals(that.displayURL) : that.displayURL != null) return false;
		if (expandedURL != null ? !expandedURL.toString().equalsIgnoreCase(that.expandedURL.toString())
				: that.expandedURL != null) return false;
		if (url != null ? !url.toString().equalsIgnoreCase(that.url.toString()) : that.url != null) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDisplayURL() {
		return displayURL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getEnd() {
		return end;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URL getExpandedURL() {
		return expandedURL;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getStart() {
		return start;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public URL getURL() {
		return url;
	}

	@Override
	public int hashCode() {
		int result = start;
		result = 31 * result + end;
		result = 31 * result + (url != null ? url.toString().hashCode() : 0);
		result = 31 * result + (expandedURL != null ? expandedURL.toString().hashCode() : 0);
		result = 31 * result + (displayURL != null ? displayURL.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "URLEntityJSONImpl{" + "start=" + start + ", end=" + end + ", url=" + url + ", expandedURL="
				+ expandedURL + ", displayURL=" + displayURL + '}';
	}

	private void init(final JSONObject json) throws TwitterException {
		try {
			final JSONArray indicesArray = json.getJSONArray("indices");
			start = indicesArray.getInt(0);
			end = indicesArray.getInt(1);

			try {
				url = new URL(json.getString("url"));
			} catch (final MalformedURLException ignore) {
			}

			if (!json.isNull("expanded_url")) {
				try {
					expandedURL = new URL(json.getString("expanded_url"));
				} catch (final MalformedURLException ignore) {
				}
			}
			if (!json.isNull("display_url")) {
				displayURL = json.getString("display_url");
			}
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}
}
