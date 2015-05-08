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

import org.mariotaku.simplerestapi.Utils;
import org.mariotaku.simplerestapi.http.SimpleValueMap;

public final class StatusUpdate extends SimpleValueMap {

    public StatusUpdate(final String status) {
        put("status", status);
    }

    public StatusUpdate displayCoordinates(final boolean displayCoordinates) {
        setDisplayCoordinates(displayCoordinates);
        return this;
    }

    public void setInReplyToStatusId(final long inReplyToStatusId) {
        put("in_reply_to_status_id", inReplyToStatusId);
    }

    public void setLocation(final GeoLocation location) {
        remove("lat");
        remove("long");
        if (location == null) return;
        put("lat", location.getLatitude());
        put("long", location.getLongitude());
    }

    public void setMediaIds(final long... mediaIds) {
        remove("media_ids");
        if (mediaIds == null) return;
        put("media_ids", Utils.toString(mediaIds, ','));
    }

    public void setPlaceId(final String placeId) {
        put("place_id", placeId);
    }

    public StatusUpdate inReplyToStatusId(final long inReplyToStatusId) {
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

    public StatusUpdate mediaIds(final long... mediaIds) {
        setMediaIds(mediaIds);
        return this;
    }

    public StatusUpdate placeId(final String placeId) {
        setPlaceId(placeId);
        return this;
    }

    public StatusUpdate possiblySensitive(final boolean possiblySensitive) {
        setPossiblySensitive(possiblySensitive);
        return this;
    }


}
