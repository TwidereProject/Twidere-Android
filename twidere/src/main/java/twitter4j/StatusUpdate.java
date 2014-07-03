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
import twitter4j.internal.util.InternalStringUtil;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.1
 */
public final class StatusUpdate implements Serializable {

	private static final long serialVersionUID = -2522880289943829826L;
	private final String status;
	private long inReplyToStatusId = -1l;
	private GeoLocation location = null;
	private String placeId = null;
	private boolean displayCoordinates = true;
	private boolean possiblySensitive;
	private String mediaName;
	private transient InputStream mediaBody;
	private File mediaFile;
	private String overrideContentType;
	private long[] mediaIds;

	public StatusUpdate(final String status) {
		this.status = status;
	}

	public StatusUpdate displayCoordinates(final boolean displayCoordinates) {
		setDisplayCoordinates(displayCoordinates);
		return this;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof StatusUpdate)) return false;
		final StatusUpdate other = (StatusUpdate) obj;
		if (displayCoordinates != other.displayCoordinates) return false;
		if (inReplyToStatusId != other.inReplyToStatusId) return false;
		if (location == null) {
			if (other.location != null) return false;
		} else if (!location.equals(other.location)) return false;
		if (mediaFile == null) {
			if (other.mediaFile != null) return false;
		} else if (!mediaFile.equals(other.mediaFile)) return false;
		if (mediaName == null) {
			if (other.mediaName != null) return false;
		} else if (!mediaName.equals(other.mediaName)) return false;
		if (overrideContentType == null) {
			if (other.overrideContentType != null) return false;
		} else if (!overrideContentType.equals(other.overrideContentType)) return false;
		if (placeId == null) {
			if (other.placeId != null) return false;
		} else if (!placeId.equals(other.placeId)) return false;
		if (possiblySensitive != other.possiblySensitive) return false;
		if (status == null) {
			if (other.status != null) return false;
		} else if (!status.equals(other.status)) return false;
		return true;
	}

	public long getInReplyToStatusId() {
		return inReplyToStatusId;
	}

	public GeoLocation getLocation() {
		return location;
	}

	public long[] getMediaIds() {
		return mediaIds;
	}

	public String getPlaceId() {
		return placeId;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (displayCoordinates ? 1231 : 1237);
		result = prime * result + (int) (inReplyToStatusId ^ inReplyToStatusId >>> 32);
		result = prime * result + (location == null ? 0 : location.hashCode());
		result = prime * result + (mediaFile == null ? 0 : mediaFile.hashCode());
		result = prime * result + (mediaName == null ? 0 : mediaName.hashCode());
		result = prime * result + (overrideContentType == null ? 0 : overrideContentType.hashCode());
		result = prime * result + (placeId == null ? 0 : placeId.hashCode());
		result = prime * result + (possiblySensitive ? 1231 : 1237);
		result = prime * result + (status == null ? 0 : status.hashCode());
		return result;
	}

	public StatusUpdate inReplyToStatusId(final long inReplyToStatusId) {
		setInReplyToStatusId(inReplyToStatusId);
		return this;
	}

	public boolean isDisplayCoordinates() {
		return displayCoordinates;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public boolean isPossiblySensitive() {
		return possiblySensitive;
	}

	public StatusUpdate location(final GeoLocation location) {
		setLocation(location);
		return this;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public StatusUpdate media(final File file, final String type) {
		setMedia(file, type);
		return this;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public StatusUpdate media(final String name, final InputStream body, final String type) {
		setMedia(name, body, type);
		return this;
	}

	public StatusUpdate mediaIds(final long... mediaIds) {
		setMediaIds(mediaIds);
		return this;
	}

	public StatusUpdate placeId(final String placeId) {
		setPlaceId(placeId);
		return this;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public StatusUpdate possiblySensitive(final boolean possiblySensitive) {
		setPossiblySensitive(possiblySensitive);
		return this;
	}

	public void setDisplayCoordinates(final boolean displayCoordinates) {
		this.displayCoordinates = displayCoordinates;
	}

	public void setInReplyToStatusId(final long inReplyToStatusId) {
		this.inReplyToStatusId = inReplyToStatusId;
	}

	public void setLocation(final GeoLocation location) {
		this.location = location;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public void setMedia(final File file, final String type) {
		mediaFile = file;
		overrideContentType = type;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public void setMedia(final String name, final InputStream body, final String type) {
		mediaName = name;
		mediaBody = body;
		overrideContentType = type;
	}

	public void setMediaIds(final long... mediaIds) {
		this.mediaIds = mediaIds;
	}

	public void setPlaceId(final String placeId) {
		this.placeId = placeId;
	}

	/**
	 * @since Twitter4J 2.2.5
	 */
	public void setPossiblySensitive(final boolean possiblySensitive) {
		this.possiblySensitive = possiblySensitive;
	}

	@Override
	public String toString() {
		return "StatusUpdate{status=" + status + ", inReplyToStatusId=" + inReplyToStatusId + ", location=" + location
				+ ", placeId=" + placeId + ", displayCoordinates=" + displayCoordinates + ", possiblySensitive="
				+ possiblySensitive + ", mediaName=" + mediaName + ", mediaFile=" + mediaFile
				+ ", overrideContentType=" + overrideContentType + "}";
	}

	private void appendParameter(final String name, final double value, final List<HttpParameter> params) {
		params.add(new HttpParameter(name, String.valueOf(value)));
	}

	private void appendParameter(final String name, final long value, final List<HttpParameter> params) {
		params.add(new HttpParameter(name, String.valueOf(value)));
	}

	private void appendParameter(final String name, final String value, final List<HttpParameter> params) {
		if (value != null) {
			params.add(new HttpParameter(name, value));
		}
	}

	/* package */HttpParameter[] asHttpParameterArray(final HttpParameter includeEntities) {
		final ArrayList<HttpParameter> params = new ArrayList<HttpParameter>();
		appendParameter("status", status, params);
		if (-1 != inReplyToStatusId) {
			appendParameter("in_reply_to_status_id", inReplyToStatusId, params);
		}
		if (location != null) {
			appendParameter("lat", location.getLatitude(), params);
			appendParameter("long", location.getLongitude(), params);
		}
		appendParameter("place_id", placeId, params);
		if (!displayCoordinates) {
			appendParameter("display_coordinates", "false", params);
		}
		params.add(includeEntities);
		if (null != mediaFile) {
			params.add(new HttpParameter("media[]", mediaFile, overrideContentType));
			params.add(new HttpParameter("possibly_sensitive", possiblySensitive));
		} else if (mediaName != null && mediaBody != null) {
			params.add(new HttpParameter("media[]", mediaName, mediaBody, overrideContentType));
			params.add(new HttpParameter("possibly_sensitive", possiblySensitive));
		} else if (mediaIds != null) {
			params.add(new HttpParameter("media_ids", InternalStringUtil.join(mediaIds)));
		}

		final HttpParameter[] paramArray = new HttpParameter[params.size()];
		return params.toArray(paramArray);
	}

	/* package */boolean isWithMedia() {
		return mediaFile != null || mediaName != null && mediaBody != null;
	}
}
