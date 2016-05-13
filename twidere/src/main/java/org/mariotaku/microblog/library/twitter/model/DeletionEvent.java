package org.mariotaku.microblog.library.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/2/26.
 */
@JsonObject
public class DeletionEvent {

    @JsonField(name = "id")
    String id;
    @JsonField(name = "user_id")
    String userId;
    @JsonField(name = "timestamp_ms")
    long timestampMs;

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

}
