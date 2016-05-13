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

        @JsonField(name = "message")
        Message message;


        @ParcelablePlease
        @JsonObject
        public static class Message implements Parcelable {

            @JsonField(name = "id")
            long id;

            @JsonField(name = "time")
            long time;

            @JsonField(name = "conversation_id")
            String conversationId;

            public String getConversationId() {
                return conversationId;
            }

            public long getId() {
                return id;
            }

            public long getTime() {
                return time;
            }

            @JsonObject
            public static class Data implements EntitySupport {

                @JsonField(name = "id")
                long id;

                @JsonField(name = "time")
                long time;
                @JsonField(name = "sender_id")
                long senderId;
                @JsonField(name = "recipient_id")
                long recipientId;
                @JsonField(name = "text")
                String text;
                @JsonField(name = "entities")
                Entities entities;
                @JsonField(name = "attachment")
                Attachment attachment;

                public String getText() {
                    return text;
                }

                public long getRecipientId() {
                    return recipientId;
                }

                public long getSenderId() {
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

                @JsonObject
                public static class Attachment {
                    @JsonField(name = "photo")
                    MediaEntity photo;

                    public MediaEntity getPhoto() {
                        return photo;
                    }
                }
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

        @StringDef({Type.ONE_TO_ONE, Type.GROUP_DM})
        public @interface Type {
            String ONE_TO_ONE = "one_to_one", GROUP_DM = "group_dm";
        }

        @ParcelablePlease
        @JsonObject
        public static class Participant implements Parcelable {

            @JsonField(name = "user_id")
            long userId;

            public long getUserId() {
                return userId;
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
