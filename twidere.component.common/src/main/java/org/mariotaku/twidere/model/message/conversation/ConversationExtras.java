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

package org.mariotaku.twidere.model.message.conversation;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.LoganSquare;

import org.mariotaku.twidere.model.ParcelableMessageConversation.ExtrasType;

import java.io.IOException;

/**
 * Created by mariotaku on 2017/2/13.
 */

public abstract class ConversationExtras implements Parcelable {
    public static ConversationExtras parse(@NonNull final String extrasType, @Nullable final String json) throws IOException {
        if (json == null) return null;
        switch (extrasType) {
            case ExtrasType.TWITTER_OFFICIAL: {
                return LoganSquare.parse(json, TwitterOfficialConversationExtras.class);
            }
        }
        return null;
    }
}
