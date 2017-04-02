/*
 *         Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mariotaku.microblog.library.twitter.model;

import org.mariotaku.restfu.RestFuUtils;
import org.mariotaku.restfu.http.SimpleValueMap;

public class StatusUpdate extends SimpleValueMap {

    public StatusUpdate(final String status) {
        put("status", status);
    }

    public void setInReplyToStatusId(final String inReplyToStatusId) {
        put("in_reply_to_status_id", inReplyToStatusId);
    }

    public void setRepostStatusId(final String repostStatusId) {
        put("repost_status_id", repostStatusId);
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


    public StatusUpdate displayCoordinates(final boolean displayCoordinates) {
        put("display_coordinates", displayCoordinates);
        return this;
    }

    public StatusUpdate autoPopulateReplyMetadata(final boolean autoPopulateReplyMetadata) {
        put("auto_populate_reply_metadata", autoPopulateReplyMetadata);
        return this;
    }

    public StatusUpdate excludeReplyUserIds(final String[] ids) {
        put("exclude_reply_userids", RestFuUtils.toString(ids, ','));
        return this;
    }

    public StatusUpdate location(final GeoLocation location) {
        remove("lat");
        remove("long");
        if (location == null) return this;
        put("lat", location.getLatitude());
        put("long", location.getLongitude());
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
        put("possibly_sensitive", possiblySensitive);
        return this;
    }


}
