package org.mariotaku.twidere.model;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations;

import java.util.Arrays;

/**
 * Created by mariotaku on 16/6/6.
 */
@CursorObject(tableInfo = true, valuesCreator = true)
public class ParcelableMessageConversation {
    @CursorField(value = Conversations._ID, type = TwidereDataStore.TYPE_PRIMARY_KEY)
    public long _id;

    @CursorField(value = Conversations.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;

    @CursorField(Conversations.CONVERSATION_ID)
    public String id;

    @CursorField(Conversations.MESSAGE_TYPE)
    public String message_type;

    @CursorField(value = Conversations.MESSAGE_TIMESTAMP)
    public long message_timestamp;

    @CursorField(value = Conversations.LOCAL_TIMESTAMP)
    public long local_timestamp;

    @CursorField(Conversations.TEXT_UNESCAPED)
    public String text_unescaped;
    @CursorField(value = Conversations.MEDIA, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableMedia[] media;
    @CursorField(value = Conversations.SPANS, converter = LoganSquareCursorFieldConverter.class)
    public SpanItem[] spans;

    @CursorField(value = Conversations.EXTRAS)
    public String extras;

    @CursorField(value = Conversations.PARTICIPANTS, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUser[] participants;

    @CursorField(value = Conversations.SENDER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey sender_key;
    @CursorField(value = Conversations.RECIPIENT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey recipient_key;

    @CursorField(Conversations.IS_OUTGOING)
    public boolean is_outgoing;

    @CursorField(value = Conversations.REQUEST_CURSOR)
    public String request_cursor;

    @Override
    public String toString() {
        return "ParcelableMessageConversation{" +
                "_id=" + _id +
                ", account_key=" + account_key +
                ", id='" + id + '\'' +
                ", message_type='" + message_type + '\'' +
                ", message_timestamp=" + message_timestamp +
                ", text_unescaped='" + text_unescaped + '\'' +
                ", media=" + Arrays.toString(media) +
                ", spans=" + Arrays.toString(spans) +
                ", participants=" + Arrays.toString(participants) +
                ", sender_key=" + sender_key +
                ", recipient_key=" + recipient_key +
                ", request_cursor='" + request_cursor + '\'' +
                '}';
    }
}
