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

import org.mariotaku.simplerestapi.http.ValueMap;

import java.util.Arrays;

import twitter4j.internal.util.InternalStringUtil;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.1
 */
public final class StatusUpdate implements ValueMap {

    private final String status;
    private long inReplyToStatusId = -1l;
    private GeoLocation location = null;
    private String placeId = null;
    private boolean displayCoordinates = true;
    private boolean possiblySensitive;
    private long[] mediaIds;

    public StatusUpdate(final String status) {
        this.status = status;
    }

    public StatusUpdate displayCoordinates(final boolean displayCoordinates) {
        setDisplayCoordinates(displayCoordinates);
        return this;
    }

    public long getInReplyToStatusId() {
        return inReplyToStatusId;
    }

    public void setInReplyToStatusId(final long inReplyToStatusId) {
        this.inReplyToStatusId = inReplyToStatusId;
    }

    public GeoLocation getLocation() {
        return location;
    }

    public void setLocation(final GeoLocation location) {
        this.location = location;
    }

    public long[] getMediaIds() {
        return mediaIds;
    }

    public void setMediaIds(final long... mediaIds) {
        this.mediaIds = mediaIds;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(final String placeId) {
        this.placeId = placeId;
    }

    public String getStatus() {
        return status;
    }

    public StatusUpdate inReplyToStatusId(final long inReplyToStatusId) {
        setInReplyToStatusId(inReplyToStatusId);
        return this;
    }

    public boolean isDisplayCoordinates() {
        return displayCoordinates;
    }

    public void setDisplayCoordinates(final boolean displayCoordinates) {
        this.displayCoordinates = displayCoordinates;
    }

    /**
     * @since Twitter4J 2.2.5
     */
    public boolean isPossiblySensitive() {
        return possiblySensitive;
    }

    /**
     * @since Twitter4J 2.2.5
     */
    public void setPossiblySensitive(final boolean possiblySensitive) {
        this.possiblySensitive = possiblySensitive;
    }

    public StatusUpdate location(final GeoLocation location) {
        setLocation(location);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatusUpdate that = (StatusUpdate) o;

        if (displayCoordinates != that.displayCoordinates) return false;
        if (inReplyToStatusId != that.inReplyToStatusId) return false;
        if (possiblySensitive != that.possiblySensitive) return false;
        if (location != null ? !location.equals(that.location) : that.location != null)
            return false;
        if (!Arrays.equals(mediaIds, that.mediaIds)) return false;
        if (placeId != null ? !placeId.equals(that.placeId) : that.placeId != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (int) (inReplyToStatusId ^ (inReplyToStatusId >>> 32));
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (placeId != null ? placeId.hashCode() : 0);
        result = 31 * result + (displayCoordinates ? 1 : 0);
        result = 31 * result + (possiblySensitive ? 1 : 0);
        result = 31 * result + (mediaIds != null ? Arrays.hashCode(mediaIds) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StatusUpdate{" +
                "status='" + status + '\'' +
                ", inReplyToStatusId=" + inReplyToStatusId +
                ", location=" + location +
                ", placeId='" + placeId + '\'' +
                ", displayCoordinates=" + displayCoordinates +
                ", possiblySensitive=" + possiblySensitive +
                ", mediaIds=" + Arrays.toString(mediaIds) +
                '}';
    }

    @Override
    public boolean has(String key) {
        switch (key) {
            case "status": {
                return status != null;
            }
            case "in_reply_to_status_id": {
                return inReplyToStatusId != -1;
            }
            case "lat":
            case "long": {
                return location != null;
            }
            case "place_id": {
                return placeId != null;
            }
            case "possibly_sensitive":
            case "display_coordinates": {
                return true;
            }
            case "media_ids": {
                return mediaIds != null && mediaIds.length > 0;
            }
        }
        return false;
    }

    @Override
    public String get(String key) {
        switch (key) {
            case "status": {
                return status;
            }
            case "in_reply_to_status_id": {
                return String.valueOf(inReplyToStatusId);
            }
            case "lat": {
                if (location == null) return null;
                return String.valueOf(location.getLatitude());
            }
            case "long": {
                if (location == null) return null;
                return String.valueOf(location.getLongitude());
            }
            case "place_id": {
                return placeId;
            }
            case "possibly_sensitive": {
                return String.valueOf(possiblySensitive);
            }
            case "display_coordinates": {
                return String.valueOf(displayCoordinates);
            }
            case "media_ids": {
                if (mediaIds == null) return null;
                return InternalStringUtil.join(mediaIds);
            }
        }
        return null;
    }

    @Override
    public String[] keys() {
        return new String[]{"status", "in_reply_to_status_id", "lat", "long", "place_id",
                "possibly_sensitive", "display_coordinates", "media_ids"};
    }
}
