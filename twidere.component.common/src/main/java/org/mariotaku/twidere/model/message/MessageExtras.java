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

package org.mariotaku.twidere.model.message;

import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.model.ParcelableMessage.MessageType;

import java.io.IOException;

/**
 * Created by mariotaku on 2017/2/9.
 */

@JsonObject
public abstract class MessageExtras implements Parcelable {
    public static MessageExtras parse(@NonNull final String messageType, @Nullable final String json) throws IOException {
        if (json == null) return null;
        switch (messageType) {
            case MessageType.STICKER:
                return LoganSquare.parse(json, StickerExtras.class);
            case MessageType.JOIN_CONVERSATION:
            case MessageType.PARTICIPANTS_LEAVE:
            case MessageType.PARTICIPANTS_JOIN:
                return LoganSquare.parse(json, UserArrayExtras.class);
            case MessageType.CONVERSATION_NAME_UPDATE:
            case MessageType.CONVERSATION_AVATAR_UPDATE:
                return LoganSquare.parse(json, ConversationInfoUpdatedExtras.class);
        }
        return null;
    }
}
