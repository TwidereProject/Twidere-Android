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
