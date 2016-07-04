/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.microblog.library.twitter.model;

import org.mariotaku.restfu.RestFuUtils;
import org.mariotaku.restfu.http.SimpleValueMap;

public class StatusUpdate extends SimpleValueMap {

    public StatusUpdate(final String status) {
        put("status", status);
    }

    public StatusUpdate displayCoordinates(final boolean displayCoordinates) {
        setDisplayCoordinates(displayCoordinates);
        return this;
    }

    public void setInReplyToStatusId(final String inReplyToStatusId) {
        put("in_reply_to_status_id", inReplyToStatusId);
    }

    public void setRepostStatusId(final String repostStatusId) {
        put("repost_status_id", repostStatusId);
    }

    public void setLocation(final GeoLocation location) {
        remove("lat");
        remove("long");
        if (location == null) return;
        put("lat", location.getLatitude());
        put("long", location.getLongitude());
    }

    public void setMediaIds(final String... mediaIds) {
        remove("media_ids");
        if (mediaIds == null) return;
        put("media_ids", RestFuUtils.toString(mediaIds, ','));
    }

    public void setAttachmentUrl(final String attachmentUrl) {
        put("attachment_url", attachmentUrl);
    }

    public void setPlaceId(final String placeId) {
        put("place_id", placeId);
    }

    public StatusUpdate inReplyToStatusId(final String inReplyToStatusId) {
        setInReplyToStatusId(inReplyToStatusId);
        return this;
    }


    public void setDisplayCoordinates(final boolean displayCoordinates) {
        put("display_coordinates", displayCoordinates);
    }


    public void setPossiblySensitive(final boolean possiblySensitive) {
        put("possibly_sensitive", possiblySensitive);
    }

    public StatusUpdate location(final GeoLocation location) {
        setLocation(location);
        return this;
    }

    public StatusUpdate mediaIds(final String... mediaIds) {
        setMediaIds(mediaIds);
        return this;
    }

    public StatusUpdate placeId(final String placeId) {
        setPlaceId(placeId);
        return this;
    }

    public StatusUpdate attachmentUrl(final String attachmentUrl) {
        setAttachmentUrl(attachmentUrl);
        return this;
    }

    public StatusUpdate possiblySensitive(final boolean possiblySensitive) {
        setPossiblySensitive(possiblySensitive);
        return this;
    }


}
