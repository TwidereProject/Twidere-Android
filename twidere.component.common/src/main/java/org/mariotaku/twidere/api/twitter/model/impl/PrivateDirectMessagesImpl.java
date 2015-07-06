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

package org.mariotaku.twidere.api.twitter.model.impl;

import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.api.twitter.model.HashtagEntity;
import org.mariotaku.twidere.api.twitter.model.MediaEntity;
import org.mariotaku.twidere.api.twitter.model.PrivateDirectMessages;
import org.mariotaku.twidere.api.twitter.model.UrlEntity;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserMentionEntity;

import java.util.Map;

/**
 * Created by mariotaku on 15/7/5.
 */
@JsonObject
public class PrivateDirectMessagesImpl implements PrivateDirectMessages {

    @JsonField(name = "user_inbox")
    UserInboxImpl userInbox;
    @JsonField(name = "user_events")
    UserEventsImpl userEvents;

    @Override
    public UserInbox getUserInbox() {
        return userInbox;
    }

    @Override
    public UserEvents getUserEvents() {
        return userEvents;
    }

    @JsonObject
    public static class UserInboxImpl implements UserInbox {

        @JsonField(name = "users")
        Map<String, UserImpl> users;

        @JsonField(name = "conversations")
        Map<String, ConversationImpl> conversations;

        @JsonField(name = "entries")
        MessageImpl[] entries;

        @Override
        public User getUser(long userId) {
            return users.get(String.valueOf(userId));
        }

        @Override
        public Conversation getConversation(String conversationId) {
            return conversations.get(conversationId);
        }

        @Override
        public Message[] getEntries() {
            return entries;
        }
    }


    @JsonObject
    public static class UserEventsImpl implements UserEvents {
        @JsonField(name = "cursor")
        String cursor;
        @JsonField(name = "last_seen_event_id")
        long lastSeenEventId;

        @Override
        public String getCursor() {
            return cursor;
        }

        @Override
        public long getLastSeenEventId() {
            return lastSeenEventId;
        }
    }

    @JsonObject
    public static class MessageImpl implements Message {

        @JsonObject
        public static class DataImpl implements Data {

            @Nullable
            @JsonField(name = "entities")
            EntitiesImpl entities;

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

            @Override
            public String getText() {
                return text;
            }

            @Override
            public String getConversationId() {
                return conversationId;
            }

            @Override
            public long getId() {
                return id;
            }

            @Override
            public long getRecipientId() {
                return recipientId;
            }

            @Override
            public long getSenderId() {
                return senderId;
            }

            @Override
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
    public static class ConversationImpl implements Conversation {

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
        ParticipantImpl[] participants;
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

        @Override
        public Participant[] getParticipants() {
            return participants;
        }

        @Override
        public String getConversationId() {
            return conversationId;
        }

        @Override
        public long getLastReadEventId() {
            return lastReadEventId;
        }

        @Override
        public long getMaxEntryId() {
            return maxEntryId;
        }

        @Override
        public long getMinEntryId() {
            return minEntryId;
        }

        @Override
        public boolean isNotificationsDisabled() {
            return notificationsDisabled;
        }

        @JsonObject
        public static class ParticipantImpl implements Participant {

            @JsonField(name = "user_id")
            long userId;

            @Override
            public long getUserId() {
                return userId;
            }
        }
    }

}
