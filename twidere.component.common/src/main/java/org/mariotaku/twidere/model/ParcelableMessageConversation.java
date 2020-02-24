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

package org.mariotaku.twidere.model;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.annotation.OnJsonParseComplete;
import com.bluelinelabs.logansquare.annotation.OnPreJsonSerialize;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.AfterCursorObjectCreated;
import org.mariotaku.library.objectcursor.annotation.BeforeWriteContentValues;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.message.MessageExtras;
import org.mariotaku.twidere.model.message.conversation.ConversationExtras;
import org.mariotaku.twidere.model.message.conversation.TwitterOfficialConversationExtras;
import org.mariotaku.twidere.model.util.ConversationExtrasConverter;
import org.mariotaku.twidere.model.util.MessageExtrasConverter;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.model.util.UserKeysCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

/**
 * Created by mariotaku on 16/6/6.
 */
@ParcelablePlease
@JsonObject
@CursorObject(tableInfo = true, valuesCreator = true)
public class ParcelableMessageConversation implements Parcelable {
    @CursorField(value = Conversations._ID, type = TwidereDataStore.TYPE_PRIMARY_KEY, excludeWrite = true)
    public long _id;

    @JsonField(name = "account_key")
    @CursorField(value = Conversations.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;
    @JsonField(name = "account_color")
    @CursorField(Conversations.ACCOUNT_COLOR)
    public int account_color;

    @JsonField(name = "conversation_id")
    @CursorField(Conversations.CONVERSATION_ID)
    public String id;

    @ConversationType
    @JsonField(name = "conversation_type")
    @CursorField(Conversations.CONVERSATION_TYPE)
    public String conversation_type;

    @JsonField(name = "conversation_name")
    @CursorField(Conversations.CONVERSATION_NAME)
    public String conversation_name;

    @JsonField(name = "conversation_avatar")
    @CursorField(Conversations.CONVERSATION_AVATAR)
    public String conversation_avatar;

    @ParcelableMessage.MessageType
    @JsonField(name = "message_type")
    @CursorField(Conversations.MESSAGE_TYPE)
    public String message_type;

    @JsonField(name = "timestamp")
    @CursorField(value = Conversations.MESSAGE_TIMESTAMP)
    public long message_timestamp;

    @JsonField(name = "local_timestamp")
    @CursorField(value = Conversations.LOCAL_TIMESTAMP)
    public long local_timestamp;

    @JsonField(name = "sort_id")
    @CursorField(value = Conversations.SORT_ID)
    public long sort_id;

    @JsonField(name = "text_unescaped")
    @CursorField(Conversations.TEXT_UNESCAPED)
    public String text_unescaped;

    @JsonField(name = "media")
    @CursorField(value = Conversations.MEDIA, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableMedia[] media;

    @JsonField(name = "spans")
    @CursorField(value = Conversations.SPANS, converter = LoganSquareCursorFieldConverter.class)
    public SpanItem[] spans;

    @CursorField(value = Conversations.MESSAGE_EXTRAS, converter = MessageExtrasConverter.class)
    public MessageExtras message_extras;

    @JsonField(name = "conversation_extras_type")
    @CursorField(Conversations.CONVERSATION_EXTRAS_TYPE)
    public String conversation_extras_type;

    @JsonField(name = "conversation_extras")
    @CursorField(value = Conversations.CONVERSATION_EXTRAS, converter = ConversationExtrasConverter.class)
    public ConversationExtras conversation_extras;

    @JsonField(name = "participants")
    @CursorField(value = Conversations.PARTICIPANTS, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUser[] participants;

    /**
     * Keys are sorted for string comparison
     */
    @JsonField(name = "participant_keys")
    @CursorField(value = Conversations.PARTICIPANT_KEYS, converter = UserKeysCursorFieldConverter.class)
    public UserKey[] participant_keys;

    @JsonField(name = "sender_key")
    @CursorField(value = Conversations.SENDER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey sender_key;

    @JsonField(name = "recipient_key")
    @CursorField(value = Conversations.RECIPIENT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey recipient_key;

    @JsonField(name = "is_outgoing")
    @CursorField(Conversations.IS_OUTGOING)
    public boolean is_outgoing;

    @JsonField(name = "request_cursor")
    @CursorField(value = Conversations.REQUEST_CURSOR)
    public String request_cursor;

    /**
     * Last read id of <b>current</b> user
     */
    @JsonField(name = "last_read_id")
    @CursorField(value = Conversations.LAST_READ_ID)
    @Nullable
    public String last_read_id;

    /**
     * Last read timestamp of <b>current</b> user
     */
    @JsonField(name = "last_read_timestamp")
    @CursorField(value = Conversations.LAST_READ_TIMESTAMP)
    public long last_read_timestamp;

    @JsonField(name = "unread_count")
    @CursorField(value = Conversations.UNREAD_COUNT, excludeWrite = true, excludeInfo = true)
    public long unread_count;

    /**
     * True if this is a temporary conversation, i.e. Created by user but haven't send any message
     * yet.
     */
    @JsonField(name = "is_temp")
    @CursorField(value = Conversations.IS_TEMP)
    public boolean is_temp;

    @JsonField(name = "message_extras")
    @ParcelableNoThanks
    ParcelableMessage.InternalExtras internalMessageExtras;

    @JsonField(name = "conversation_extras")
    @ParcelableNoThanks
    InternalExtras internalConversationExtras;

    @Override
    public String toString() {
        return "ParcelableMessageConversation{" +
                "_id=" + _id +
                ", account_key=" + account_key +
                ", account_color=" + account_color +
                ", id='" + id + '\'' +
                ", conversation_type='" + conversation_type + '\'' +
                ", conversation_name='" + conversation_name + '\'' +
                ", conversation_avatar='" + conversation_avatar + '\'' +
                ", message_type='" + message_type + '\'' +
                ", message_timestamp=" + message_timestamp +
                ", local_timestamp=" + local_timestamp +
                ", sort_id=" + sort_id +
                ", text_unescaped='" + text_unescaped + '\'' +
                ", media=" + Arrays.toString(media) +
                ", spans=" + Arrays.toString(spans) +
                ", message_extras=" + message_extras +
                ", conversation_extras_type='" + conversation_extras_type + '\'' +
                ", conversation_extras=" + conversation_extras +
                ", participants=" + Arrays.toString(participants) +
                ", sender_key=" + sender_key +
                ", recipient_key=" + recipient_key +
                ", is_outgoing=" + is_outgoing +
                ", request_cursor='" + request_cursor + '\'' +
                ", is_temp=" + is_temp +
                ", internalMessageExtras=" + internalMessageExtras +
                ", internalConversationExtras=" + internalConversationExtras +
                '}';
    }


    @OnPreJsonSerialize
    void beforeJsonSerialize() {
        internalMessageExtras = ParcelableMessage.InternalExtras.from(message_extras);
        internalConversationExtras = InternalExtras.from(conversation_extras);
        prepareParticipantKeys();
    }

    @OnJsonParseComplete
    void onJsonParseComplete() {
        if (internalMessageExtras != null) {
            message_extras = internalMessageExtras.getExtras();
        }
        if (internalConversationExtras != null) {
            conversation_extras = internalConversationExtras.getExtras();
        }
        if (participants != null) {
            participants = removeNullParticipants(participants);
        }
    }

    @AfterCursorObjectCreated
    void afterCursorObjectCreated() {
        if (participants != null) {
            participants = removeNullParticipants(participants);
        }
    }

    @BeforeWriteContentValues
    void beforeWriteContentValues(ContentValues values) throws IOException {
        if (participants != null) {
            participants = removeNullParticipants(participants);
        }
        prepareParticipantKeys();
    }

    private void onParcelableCreated() {
        if (participants != null) {
            participants = removeNullParticipants(participants);
        }
    }

    private ParcelableUser[] removeNullParticipants(final ParcelableUser[] participants) {
        int nullCount = 0;
        for (final ParcelableUser user : participants) {
            if (user == null) nullCount++;
        }
        if (nullCount == 0) return participants;
        final int resultLength = participants.length - nullCount;
        final ParcelableUser[] result = new ParcelableUser[resultLength];
        for (int i = 0, j = 0, l = participants.length; i < l; i++) {
            final ParcelableUser user = participants[i];
            if (user == null) continue;
            result[j++] = user;
        }
        return result;
    }

    private void prepareParticipantKeys() {
        // Ensure keys are ordered
        if (participants != null && participant_keys == null) {
            participant_keys = new UserKey[participants.length];
            for (int i = 0, j = participants.length; i < j; i++) {
                participant_keys[i] = participants[i].key;
            }
        }
        if (participant_keys != null) {
            Arrays.sort(participant_keys);
        }
    }
    @StringDef({ConversationType.ONE_TO_ONE, ConversationType.GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ConversationType {
        String ONE_TO_ONE = "one_to_one";
        String GROUP = "group";

    }
    @StringDef({ExtrasType.DEFAULT, ExtrasType.TWITTER_OFFICIAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ExtrasType {
        String DEFAULT = "default";
        String TWITTER_OFFICIAL = "twitter_official";


    }
    @JsonObject
    static class InternalExtras {

        @JsonField(name = "twitter_official")
        TwitterOfficialConversationExtras twitterOfficial;

        public static InternalExtras from(final ConversationExtras extras) {
            if (extras == null) return null;
            InternalExtras result = new InternalExtras();
            if (extras instanceof TwitterOfficialConversationExtras) {
                result.twitterOfficial = (TwitterOfficialConversationExtras) extras;
            } else {
                return null;
            }
            return result;
        }
        public ConversationExtras getExtras() {
            if (twitterOfficial != null) {
                return twitterOfficial;
            }
            return null;
        }

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableMessageConversationParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableMessageConversation> CREATOR = new Creator<ParcelableMessageConversation>() {
        public ParcelableMessageConversation createFromParcel(Parcel source) {
            ParcelableMessageConversation target = new ParcelableMessageConversation();
            ParcelableMessageConversationParcelablePlease.readFromParcel(target, source);
            target.onParcelableCreated();
            return target;
        }

        public ParcelableMessageConversation[] newArray(int size) {
            return new ParcelableMessageConversation[size];
        }
    };
}
