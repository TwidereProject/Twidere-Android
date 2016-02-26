package org.mariotaku.twidere.api.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/2/26.
 */
@JsonObject
public class DeletionEvent {

    @JsonField(name = "id")
    long id;
    @JsonField(name = "user_id")
    long userId;
    @JsonField(name = "timestamp_ms")
    long timestampMs;

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

}
