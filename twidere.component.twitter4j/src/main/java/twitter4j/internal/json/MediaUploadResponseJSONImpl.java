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

import org.json.JSONException;
import org.json.JSONObject;

import twitter4j.MediaUploadResponse;
import twitter4j.TwitterException;
import twitter4j.http.HttpResponse;

import static twitter4j.internal.util.InternalParseUtil.getInt;
import static twitter4j.internal.util.InternalParseUtil.getLong;
import static twitter4j.internal.util.InternalParseUtil.getRawString;

final class MediaUploadResponseJSONImpl extends TwitterResponseImpl implements MediaUploadResponse {

	private static final long serialVersionUID = 1024124737076048737L;

	private long id;
	private long size;

	private ImageJSONImpl image;

	public MediaUploadResponseJSONImpl(final HttpResponse res) throws TwitterException {
		super(res);
		try {
			init(res.asJSONObject());
		} catch (final JSONException e) {
			throw new TwitterException(e);
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof MediaUploadResponseJSONImpl)) return false;
		final MediaUploadResponseJSONImpl other = (MediaUploadResponseJSONImpl) obj;
		if (id != other.id) return false;
		return true;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public Image getImage() {
		return image;
	}

	@Override
	public long getSize() {
		return size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ id >>> 32);
		return result;
	}

	@Override
	public String toString() {
		return "MediaUploadResponseJSONImpl{id=" + id + ", size=" + size + ", image=" + image + "}";
	}

	private void init(final JSONObject json) throws JSONException {
		if (json.isNull("image")) {
			image = new ImageJSONImpl(json.getJSONObject("image"));
		}
		id = getLong("media_id", json);
		size = getLong("size", json);
	}

	static class ImageJSONImpl implements Image {
		private final int width;
		private final int height;
		private final String imageType;

		public ImageJSONImpl(final JSONObject json) {
			width = getInt("w", json);
			height = getInt("h", json);
			imageType = getRawString("image_type", json);
		}

		@Override
		public int getHeight() {
			return height;
		}

		@Override
		public String getImageType() {
			return imageType;
		}

		@Override
		public int getWidth() {
			return width;
		}

		@Override
		public String toString() {
			return "ImageJSONImpl{width=" + width + ", height=" + height + ", imageType=" + imageType + "}";
		}

	}

}
