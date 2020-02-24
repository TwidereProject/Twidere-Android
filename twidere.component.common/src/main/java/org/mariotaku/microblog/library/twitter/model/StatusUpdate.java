/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library.twitter.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.mariotaku.restfu.RestFuUtils;
import org.mariotaku.restfu.http.SimpleValueMap;

public class StatusUpdate extends SimpleValueMap {

    public StatusUpdate(@NonNull final String status) {
        put("status", status);
    }

    public StatusUpdate repostStatusId(final String repostStatusId) {
        put("repost_status_id", repostStatusId);
        return this;
    }

    public StatusUpdate inReplyToStatusId(final String inReplyToStatusId) {
        put("in_reply_to_status_id", inReplyToStatusId);
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

    public StatusUpdate excludeReplyUserIds(@Nullable final String[] ids) {
        if (ids == null) {
            remove("exclude_reply_user_ids");
        } else {
            put("exclude_reply_user_ids", RestFuUtils.toString(ids, ','));
        }
        return this;
    }

    public StatusUpdate location(final GeoLocation location) {
        if (location == null) {
            remove("lat");
            remove("long");
        } else {
            put("lat", location.getLatitude());
            put("long", location.getLongitude());
        }
        return this;
    }

    public StatusUpdate mediaIds(@Nullable final String[] mediaIds) {
        if (mediaIds == null) {
            remove("media_ids");
        } else {
            put("media_ids", RestFuUtils.toString(mediaIds, ','));
        }
        return this;
    }

    public StatusUpdate placeId(final String placeId) {
        put("place_id", placeId);
        return this;
    }

    public StatusUpdate attachmentUrl(final String attachmentUrl) {
        put("attachment_url", attachmentUrl);
        return this;
    }

    public StatusUpdate possiblySensitive(final boolean possiblySensitive) {
        put("possibly_sensitive", possiblySensitive);
        return this;
    }


}
