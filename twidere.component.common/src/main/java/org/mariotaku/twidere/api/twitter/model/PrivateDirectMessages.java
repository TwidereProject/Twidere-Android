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

package org.mariotaku.twidere.api.twitter.model;

import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.Map;

/**
 * Created by mariotaku on 15/7/5.
 */
@JsonObject
public class PrivateDirectMessages {

    @JsonField(name = "user_inbox")
    UserInbox userInbox;
    @JsonField(name = "user_events")
    UserEvents userEvents;

    public UserInbox getUserInbox() {
        return userInbox;
    }

    public UserEvents getUserEvents() {
        return userEvents;
    }

    public enum Status {
        HAS_MORE, AT_END
    }

    @JsonObject
    public static class UserInbox {

        @JsonField(name = "users")
        Map<String, User> users;

        @JsonField(name = "conversations")
        Map<String, Conversation> conversations;

        @JsonField(name = "entries")
        Message[] entries;

        public User getUser(long userId) {
            return users.get(String.valueOf(userId));
        }

        public Conversation getConversation(String conversationId) {
            return conversations.get(conversationId);
        }

        public Message[] getEntries() {
            return entries;
        }
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

    @JsonObject
    public static class Message {

        @JsonObject
        public static class Data implements EntitySupport {

            @Nullable
            @JsonField(name = "entities")
            Entities entities;

            @JsonField(name = "sender_id")
            long senderId;
            @JsonField(name = "recipient_id")
            long recipientId;
            @JsonField(name = "id")
            long id;
            @JsonField(name = "conversation_id")
            String conversationId;
            @JsonField(name = "text")
            String text;
            @JsonField(name = "time")
            long time;

            public String getText() {
                return text;
            }

            public String getConversationId() {
                return conversationId;
            }

            public long getId() {
                return id;
            }

            public long getRecipientId() {
                return recipientId;
            }

            public long getSenderId() {
                return senderId;
            }

            public long getTime() {
                return time;
            }

            @Override
            public HashtagEntity[] getHashtagEntities() {
                if (entities == null) return null;
                return entities.getHashtags();
            }

            @Override
            public UrlEntity[] getUrlEntities() {
                if (entities == null) return null;
                return entities.getUrls();
            }

            @Override
            public MediaEntity[] getMediaEntities() {
                if (entities == null) return null;
                return entities.getMedia();
            }

            @Override
            public UserMentionEntity[] getUserMentionEntities() {
                if (entities == null) return null;
                return entities.getUserMentions();
            }
        }

    }

    @JsonObject
    public static class Conversation {

        @JsonField(name = "conversation_id")
        String conversationId;
        @JsonField(name = "last_read_event_id")
        long lastReadEventId;
        @JsonField(name = "max_entry_id")
        long maxEntryId;
        @JsonField(name = "min_entry_id")
        long minEntryId;
        @JsonField(name = "notifications_disabled")
        boolean notificationsDisabled;
        @JsonField(name = "participants")
        Participant[] participants;
        @JsonField(name = "read_only")
        boolean readOnly;
        @JsonField(name = "sort_event_id")
        long sortEventId;
        @JsonField(name = "sort_timestamp")
        long sortTimestamp;
        @JsonField(name = "status")
        Status status;
        @JsonField(name = "type")
        Type type;

        public Participant[] getParticipants() {
            return participants;
        }

        public String getConversationId() {
            return conversationId;
        }

        public long getLastReadEventId() {
            return lastReadEventId;
        }

        public long getMaxEntryId() {
            return maxEntryId;
        }

        public long getMinEntryId() {
            return minEntryId;
        }

        public boolean isNotificationsDisabled() {
            return notificationsDisabled;
        }

        public enum Type {
            ONE_TO_ONE("one_to_one"), GROUP_DM("group_dm");

            private final String literal;

            Type(String literal) {
                this.literal = literal;
            }
        }

        @JsonObject
        public static class Participant {

            @JsonField(name = "user_id")
            long userId;

            public long getUserId() {
                return userId;
            }
        }
    }

}
