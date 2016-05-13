package org.mariotaku.microblog.library.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 16/3/1.
 */
@JsonObject
public class UserEventsResponse extends TwitterResponseObject {
    @JsonField(name = "user_events")
    UserEvents userEvents;

    public UserEvents getUserEvents() {
        return userEvents;
    }

    @JsonObject
    public static class UserEvents {
        @JsonField(name = "cursor")
        String cursor;
        @JsonField(name = "last_seen_event_id")
        long lastSeenEventId;

        public String getCursor() {
            return cursor;
        }

        public long getLastSeenEventId() {
            return lastSeenEventId;
        }
    }

}
