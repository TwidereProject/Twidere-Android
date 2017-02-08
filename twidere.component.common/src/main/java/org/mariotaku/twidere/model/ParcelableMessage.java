package org.mariotaku.twidere.model;

import android.support.annotation.StringDef;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.Messages;

import java.util.Arrays;

/**
 * Created by mariotaku on 16/6/6.
 */
@CursorObject(tableInfo = true, valuesCreator = true)
public class ParcelableMessage {
    @CursorField(value = Messages._ID, type = TwidereDataStore.TYPE_PRIMARY_KEY, excludeWrite = true)
    public long _id;

    @CursorField(value = Messages.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;

    @CursorField(Messages.MESSAGE_ID)
    public String id;

    @CursorField(Messages.CONVERSATION_ID)
    public String conversation_id;

    @CursorField(Messages.MESSAGE_TYPE)
    @Type
    public String message_type;

    @CursorField(Messages.MESSAGE_TIMESTAMP)
    public long message_timestamp;

    @CursorField(Messages.LOCAL_TIMESTAMP)
    public long local_timestamp;

    @CursorField(Messages.TEXT_UNESCAPED)
    public String text_unescaped;
    @CursorField(value = Messages.MEDIA, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableMedia[] media;
    @CursorField(value = Messages.SPANS, converter = LoganSquareCursorFieldConverter.class)
    public SpanItem[] spans;
    @CursorField(value = Messages.EXTRAS)
    public String extras;

    @CursorField(value = Messages.SENDER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey sender_key;
    @CursorField(value = Messages.RECIPIENT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey recipient_key;

    @CursorField(Messages.IS_OUTGOING)
    public boolean is_outgoing;

    @CursorField(value = Messages.REQUEST_CURSOR)
    public String request_cursor;

    @Override
    public String toString() {
        return "ParcelableMessage{" +
                "_id=" + _id +
                ", account_key=" + account_key +
                ", id='" + id + '\'' +
                ", conversation_id='" + conversation_id + '\'' +
                ", message_type='" + message_type + '\'' +
                ", message_timestamp=" + message_timestamp +
                ", text_unescaped='" + text_unescaped + '\'' +
                ", media=" + Arrays.toString(media) +
                ", spans=" + Arrays.toString(spans) +
                ", sender_key=" + sender_key +
                ", recipient_key=" + recipient_key +
                ", is_outgoing=" + is_outgoing +
                ", request_cursor='" + request_cursor + '\'' +
                '}';
    }

    @StringDef({Type.TEXT, Type.STICKER})
    public @interface Type {
        String TEXT = "text";
        String STICKER = "sticker";
    }
}
