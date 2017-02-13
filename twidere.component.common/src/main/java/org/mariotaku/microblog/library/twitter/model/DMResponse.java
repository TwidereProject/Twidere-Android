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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.Bagger;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.twitter.model.util.ParcelMapBagger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by mariotaku on 16/3/1.
 */
@ParcelablePlease
@JsonObject
public class DMResponse implements Parcelable {

    @JsonField(name = "status")
    @Status
    String status;
    @JsonField(name = "cursor")
    String cursor;

    @JsonField(name = "min_entry_id")
    long minEntryId;

    @JsonField(name = "max_entry_id")
    long maxEntryId;

    @JsonField(name = "last_seen_event_id")
    long lastSeenEvent;

    @JsonField(name = "users")
    @Bagger(UserMapBagger.class)
    Map<String, User> users;

    @JsonField(name = "conversations")
    @Bagger(ConversationMapBagger.class)
    Map<String, Conversation> conversations;

    @JsonField(name = "entries")
    Entry[] entries;

    public String getStatus() {
        return status;
    }

    public String getCursor() {
        return cursor;
    }

    public long getMinEntryId() {
        return minEntryId;
    }

    public long getMaxEntryId() {
        return maxEntryId;
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public Map<String, Conversation> getConversations() {
        return conversations;
    }

    public User getUser(long userId) {
        return users.get(String.valueOf(userId));
    }

    public Conversation getConversation(String conversationId) {
        return conversations.get(conversationId);
    }

    public Entry[] getEntries() {
        return entries;
    }

    @StringDef({DMResponse.Status.HAS_MORE, DMResponse.Status.AT_END})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {
        String HAS_MORE = "HAS_MORE";
        String AT_END = "AT_END";
    }

    @ParcelablePlease
    @JsonObject
    public static class Entry implements Parcelable {

        @JsonField(name = "conversation_create")
        Message conversationCreate;
        @JsonField(name = "join_conversation")
        Message joinConversation;
        @JsonField(name = "message")
        Message message;
        @JsonField(name = "participants_leave")
        Message participantsLeave;
        @JsonField(name = "participants_join")
        Message participantsJoin;
        @JsonField(name = "conversation_name_update")
        Message conversationNameUpdate;

        public Message getJoinConversation() {
            return joinConversation;
        }

        public Message getConversationCreate() {
            return conversationCreate;
        }

        public Message getMessage() {
            return message;
        }

        public Message getParticipantsLeave() {
            return participantsLeave;
        }

        public Message getParticipantsJoin() {
            return participantsJoin;
        }

        public Message getConversationNameUpdate() {
            return conversationNameUpdate;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "conversationCreate=" + conversationCreate +
                    ", joinConversation=" + joinConversation +
                    ", message=" + message +
                    ", participantsLeave=" + participantsLeave +
                    ", participantsJoin=" + participantsJoin +
                    ", conversationNameUpdate=" + conversationNameUpdate +
                    '}';
        }

        @ParcelablePlease
        @JsonObject
        public static class Message implements Parcelable {

            @JsonField(name = "id")
            long id;

            @JsonField(name = "time")
            long time;

            @JsonField(name = "affects_sort")
            boolean affectsSort;

            @JsonField(name = "conversation_id")
            String conversationId;

            @JsonField(name = "conversation_name")
            String conversationName;

            @JsonField(name = "request_id")
            String requestId;

            @JsonField(name = "sender_id")
            String senderId;

            @JsonField(name = "by_user_id")
            String byUserId;

            @JsonField(name = "message_data")
            Data messageData;

            @JsonField(name = "participants")
            Conversation.Participant[] participants;

            public boolean isAffectsSort() {
                return affectsSort;
            }

            public String getConversationId() {
                return conversationId;
            }

            public long getId() {
                return id;
            }

            public long getTime() {
                return time;
            }

            public String getRequestId() {
                return requestId;
            }

            public String getSenderId() {
                return senderId;
            }

            public Data getMessageData() {
                return messageData;
            }

            public Conversation.Participant[] getParticipants() {
                return participants;
            }

            public String getConversationName() {
                return conversationName;
            }

            public String getByUserId() {
                return byUserId;
            }

            @Override
            public String toString() {
                return "Message{" +
                        "affectsSort=" + affectsSort +
                        ", id=" + id +
                        ", time=" + time +
                        ", conversationId='" + conversationId + '\'' +
                        ", conversationName='" + conversationName + '\'' +
                        ", requestId='" + requestId + '\'' +
                        ", senderId='" + senderId + '\'' +
                        ", byUserId='" + byUserId + '\'' +
                        ", messageData=" + messageData +
                        ", participants=" + Arrays.toString(participants) +
                        '}';
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                DMResponse$Entry$MessageParcelablePlease.writeToParcel(this, dest, flags);
            }

            public static final Creator<Message> CREATOR = new Creator<Message>() {
                @Override
                public Message createFromParcel(Parcel source) {
                    Message target = new Message();
                    DMResponse$Entry$MessageParcelablePlease.readFromParcel(target, source);
                    return target;
                }

                @Override
                public Message[] newArray(int size) {
                    return new Message[size];
                }
            };

            @ParcelablePlease
            @JsonObject
            public static class Data implements EntitySupport, Parcelable {

                @JsonField(name = "id")
                long id;

                @JsonField(name = "time")
                long time;
                @JsonField(name = "sender_id")
                String senderId;
                @JsonField(name = "recipient_id")
                String recipientId;
                @JsonField(name = "text")
                String text;
                @JsonField(name = "entities")
                Entities entities;
                @JsonField(name = "attachment")
                Attachment attachment;

                public String getText() {
                    return text;
                }

                public String getRecipientId() {
                    return recipientId;
                }

                public String getSenderId() {
                    return senderId;
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

                public Attachment getAttachment() {
                    return attachment;
                }

                public Entities getEntities() {
                    return entities;
                }

                public long getTime() {
                    return time;
                }

                public long getId() {
                    return id;
                }

                @ParcelablePlease
                @JsonObject
                public static class Attachment implements Parcelable {
                    @JsonField(name = "photo")
                    MediaEntity photo;
                    @JsonField(name = "sticker")
                    StickerEntity sticker;

                    public MediaEntity getPhoto() {
                        return photo;
                    }

                    public StickerEntity getSticker() {
                        return sticker;
                    }

                    @Override
                    public String toString() {
                        return "Attachment{" +
                                "photo=" + photo +
                                ", sticker=" + sticker +
                                '}';
                    }

                    @Override
                    public int describeContents() {
                        return 0;
                    }

                    @Override
                    public void writeToParcel(Parcel dest, int flags) {
                        DMResponse$Entry$Message$Data$AttachmentParcelablePlease.writeToParcel(this, dest, flags);
                    }

                    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
                        public Attachment createFromParcel(Parcel source) {
                            Attachment target = new Attachment();
                            DMResponse$Entry$Message$Data$AttachmentParcelablePlease.readFromParcel(target, source);
                            return target;
                        }

                        public Attachment[] newArray(int size) {
                            return new Attachment[size];
                        }
                    };
                }

                @Override
                public int describeContents() {
                    return 0;
                }

                @Override
                public void writeToParcel(Parcel dest, int flags) {
                    DMResponse$Entry$Message$DataParcelablePlease.writeToParcel(this, dest, flags);
                }

                public static final Creator<Data> CREATOR = new Creator<Data>() {
                    public Data createFromParcel(Parcel source) {
                        Data target = new Data();
                        DMResponse$Entry$Message$DataParcelablePlease.readFromParcel(target, source);
                        return target;
                    }

                    public Data[] newArray(int size) {
                        return new Data[size];
                    }
                };
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            DMResponse$EntryParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Entry> CREATOR = new Creator<Entry>() {
            @Override
            public Entry createFromParcel(Parcel source) {
                Entry target = new Entry();
                DMResponse$EntryParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public Entry[] newArray(int size) {
                return new Entry[size];
            }
        };
    }

    @ParcelablePlease
    @JsonObject
    public static class Conversation implements Parcelable {

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
        @Status
        String status;
        @JsonField(name = "type")
        @Type
        String type;

        @JsonField(name = "created_by_user_id")
        String createdByUserId;

        @JsonField(name = "created_time")
        String createdTime;

        @JsonField(name = "name")
        String name;

        public String getType() {
            return type;
        }

        public String getStatus() {
            return status;
        }

        public long getSortTimestamp() {
            return sortTimestamp;
        }

        public long getSortEventId() {
            return sortEventId;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

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

        public String getCreatedByUserId() {
            return createdByUserId;
        }

        public String getCreatedTime() {
            return createdTime;
        }

        public String getName() {
            return name;
        }

        @StringDef({Type.ONE_TO_ONE, Type.GROUP_DM})
        @Retention(RetentionPolicy.SOURCE)
        public @interface Type {
            String ONE_TO_ONE = "ONE_TO_ONE", GROUP_DM = "GROUP_DM";
        }

        @ParcelablePlease
        @JsonObject
        public static class Participant implements Parcelable {

            @JsonField(name = "user_id")
            String userId;
            @JsonField(name = "join_time")
            long joinTime;

            public String getUserId() {
                return userId;
            }

            public long getJoinTime() {
                return joinTime;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                DMResponse$Conversation$ParticipantParcelablePlease.writeToParcel(this, dest, flags);
            }

            public static final Creator<Participant> CREATOR = new Creator<Participant>() {
                @Override
                public Participant createFromParcel(Parcel source) {
                    Participant target = new Participant();
                    DMResponse$Conversation$ParticipantParcelablePlease.readFromParcel(target, source);
                    return target;
                }

                @Override
                public Participant[] newArray(int size) {
                    return new Participant[size];
                }
            };
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            DMResponse$ConversationParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
            @Override
            public Conversation createFromParcel(Parcel source) {
                Conversation target = new Conversation();
                DMResponse$ConversationParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public Conversation[] newArray(int size) {
                return new Conversation[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        DMResponseParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<DMResponse> CREATOR = new Creator<DMResponse>() {
        @Override
        public DMResponse createFromParcel(Parcel source) {
            DMResponse target = new DMResponse();
            DMResponseParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public DMResponse[] newArray(int size) {
            return new DMResponse[size];
        }
    };

    public static class UserMapBagger extends ParcelMapBagger<User> {
        public UserMapBagger() {
            super(User.class);
        }
    }

    public static class ConversationMapBagger extends ParcelMapBagger<Conversation> {
        public ConversationMapBagger() {
            super(Conversation.class);
        }
    }
}
