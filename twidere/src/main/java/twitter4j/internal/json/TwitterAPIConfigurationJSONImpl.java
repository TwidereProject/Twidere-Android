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

import static twitter4j.internal.util.InternalParseUtil.getInt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.MediaEntity;
import twitter4j.TwitterAPIConfiguration;
import twitter4j.TwitterException;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.2.3
 */
class TwitterAPIConfigurationJSONImpl extends TwitterResponseImpl implements TwitterAPIConfiguration {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6112937720837291428L;
	private int photoSizeLimit;
	private int shortURLLength;
	private int shortURLLengthHttps;

	private int charactersReservedPerMedia;
	private Map<Integer, MediaEntity.Size> photoSizes;
	private String[] nonUsernamePaths;
	private int maxMediaPerUpload;

	TwitterAPIConfigurationJSONImpl(final HttpResponse res, final Configuration conf) throws TwitterException {
		super(res);
		try {
			final JSONObject json = res.asJSONObject();
			photoSizeLimit = getInt("photo_size_limit", json);
			shortURLLength = getInt("short_url_length", json);
			shortURLLengthHttps = getInt("short_url_length_https", json);
			charactersReservedPerMedia = getInt("characters_reserved_per_media", json);

			final JSONObject sizes = json.getJSONObject("photo_sizes");
			photoSizes = new HashMap<Integer, MediaEntity.Size>(4);
			photoSizes.put(MediaEntity.Size.LARGE, new MediaEntityJSONImpl.Size(sizes.getJSONObject("large")));
			JSONObject medium;
			// http://code.google.com/p/twitter-api/issues/detail?id=2230
			if (sizes.isNull("med")) {
				medium = sizes.getJSONObject("medium");
			} else {
				medium = sizes.getJSONObject("med");
			}
			photoSizes.put(MediaEntity.Size.MEDIUM, new MediaEntityJSONImpl.Size(medium));
			photoSizes.put(MediaEntity.Size.SMALL, new MediaEntityJSONImpl.Size(sizes.getJSONObject("small")));
			photoSizes.put(MediaEntity.Size.THUMB, new MediaEntityJSONImpl.Size(sizes.getJSONObject("thumb")));
			final JSONArray nonUsernamePathsJSONArray = json.getJSONArray("non_username_paths");
			nonUsernamePaths = new String[nonUsernamePathsJSONArray.length()];
			for (int i = 0; i < nonUsernamePathsJSONArray.length(); i++) {
				nonUsernamePaths[i] = nonUsernamePathsJSONArray.getString(i);
			}
			maxMediaPerUpload = getInt("max_media_per_upload", json);
		} catch (final JSONException jsone) {
			throw new TwitterException(jsone);
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof TwitterAPIConfigurationJSONImpl)) return false;

		final TwitterAPIConfigurationJSONImpl that = (TwitterAPIConfigurationJSONImpl) o;

		if (charactersReservedPerMedia != that.charactersReservedPerMedia) return false;
		if (maxMediaPerUpload != that.maxMediaPerUpload) return false;
		if (photoSizeLimit != that.photoSizeLimit) return false;
		if (shortURLLength != that.shortURLLength) return false;
		if (shortURLLengthHttps != that.shortURLLengthHttps) return false;
		if (!Arrays.equals(nonUsernamePaths, that.nonUsernamePaths)) return false;
		if (photoSizes != null ? !photoSizes.equals(that.photoSizes) : that.photoSizes != null) return false;

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getCharactersReservedPerMedia() {
		return charactersReservedPerMedia;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getMaxMediaPerUpload() {
		return maxMediaPerUpload;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getNonUsernamePaths() {
		return nonUsernamePaths;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getPhotoSizeLimit() {
		return photoSizeLimit;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Integer, MediaEntity.Size> getPhotoSizes() {
		return photoSizes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getShortURLLength() {
		return shortURLLength;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getShortURLLengthHttps() {
		return shortURLLengthHttps;
	}

	@Override
	public int hashCode() {
		int result = photoSizeLimit;
		result = 31 * result + shortURLLength;
		result = 31 * result + shortURLLengthHttps;
		result = 31 * result + charactersReservedPerMedia;
		result = 31 * result + (photoSizes != null ? photoSizes.hashCode() : 0);
		result = 31 * result + (nonUsernamePaths != null ? Arrays.hashCode(nonUsernamePaths) : 0);
		result = 31 * result + maxMediaPerUpload;
		return result;
	}

	@Override
	public String toString() {
		return "TwitterAPIConfigurationJSONImpl{" + "photoSizeLimit=" + photoSizeLimit + ", shortURLLength="
				+ shortURLLength + ", shortURLLengthHttps=" + shortURLLengthHttps + ", charactersReservedPerMedia="
				+ charactersReservedPerMedia + ", photoSizes=" + photoSizes + ", nonUsernamePaths="
				+ (nonUsernamePaths == null ? null : Arrays.asList(nonUsernamePaths)) + ", maxMediaPerUpload="
				+ maxMediaPerUpload + '}';
	}
}
