package org.mariotaku.twidere.model;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations;

/**
 * Created by mariotaku on 16/6/6.
 */
@CursorObject(tableInfo = true, valuesCreator = true)
public class ParcelableMessageConversation {
    @CursorField(value = Conversations._ID, type = TwidereDataStore.TYPE_PRIMARY_KEY)
    public long _id;
    @CursorField(Conversations.CONVERSATION_ID)
    public String id;
    @CursorField(value = Conversations.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;
    @CursorField(value = Conversations.LAST_SEND_AT)
    public long last_send_at;
    @CursorField(Conversations.TEXT_UNESCAPED)
    public String text_unescaped;
    @CursorField(value = Conversations.MEDIA_JSON, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableMedia[] media;
    @CursorField(value = Conversations.PARTICIPANTS, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUser[] participants;
    @CursorField(value = Conversations.SENDER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey sender_key;
    @CursorField(value = Conversations.RECIPIENT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey recipient_key;
    @CursorField(value = Conversations.REQUEST_CURSOR)
    public String request_cursor;

}
