package org.mariotaku.twidere.model;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Entries;

/**
 * Created by mariotaku on 16/3/28.
 */
@CursorObject
public class ParcelableMessageEntry {

    @CursorField(value = Entries._ID, excludeWrite = true)
    public long id;
    @CursorField(value = Entries.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;
    @CursorField(value = Entries.CONVERSATION_ID)
    public String conversation_id;
    @CursorField(value = Entries.UPDATED_AT)
    public long updated_at;
    @CursorField(value = Entries.TEXT_CONTENT)
    public String text_content;

}
