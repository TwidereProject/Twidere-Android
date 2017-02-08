/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.model.util.UserKeyCursorFieldConverter;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;

@CursorObject
@Deprecated
public class ParcelableDirectMessage {

    @CursorField(value = DirectMessages._ID, type = TwidereDataStore.TYPE_PRIMARY_KEY, excludeWrite = true)
    public long _id;

    @CursorField(value = DirectMessages.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;

    @CursorField(DirectMessages.MESSAGE_ID)
    public String id;

    @CursorField(DirectMessages.MESSAGE_TIMESTAMP)
    public long timestamp;

    @CursorField(DirectMessages.SENDER_ID)
    public String sender_id;

    @CursorField(DirectMessages.RECIPIENT_ID)
    public String recipient_id;

    @CursorField(DirectMessages.IS_OUTGOING)
    public boolean is_outgoing;

    @CursorField(DirectMessages.TEXT_UNESCAPED)
    public String text_unescaped;

    @CursorField(DirectMessages.TEXT_PLAIN)
    public String text_plain;

    @CursorField(value = DirectMessages.SPANS, converter = LoganSquareCursorFieldConverter.class)
    public SpanItem[] spans;

    @CursorField(DirectMessages.SENDER_NAME)
    public String sender_name;

    @CursorField(DirectMessages.RECIPIENT_NAME)
    public String recipient_name;

    @CursorField(DirectMessages.SENDER_SCREEN_NAME)
    public String sender_screen_name;

    @CursorField(DirectMessages.RECIPIENT_SCREEN_NAME)
    public String recipient_screen_name;

    @CursorField(DirectMessages.SENDER_PROFILE_IMAGE_URL)
    public String sender_profile_image_url;

    @CursorField(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL)
    public String recipient_profile_image_url;

    @CursorField(DirectMessages.CONVERSATION_ID)
    public String conversation_id;

    @CursorField(value = DirectMessages.MEDIA_JSON, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableMedia[] media;

}
