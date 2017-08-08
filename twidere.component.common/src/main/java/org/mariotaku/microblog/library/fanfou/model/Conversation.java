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

package org.mariotaku.microblog.library.fanfou.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.twitter.model.DirectMessage;

/**
 * Fanfou DM conversation object
 * <p>
 * Created by mariotaku on 2017/2/11.
 *
 * @see <a href="https://github.com/FanfouAPI/FanFouAPIDoc/wiki/direct-messages.conversation-list">GET /privete_messages/conversation_list</a>
 */
@ParcelablePlease
@JsonObject
public class Conversation implements Parcelable {
    @JsonField(name = "dm")
    DirectMessage dm;
    @JsonField(name = "otherid")
    String otherId;
    @JsonField(name = "msg_num")
    int messageNumber;
    @JsonField(name = "new_conv")
    boolean newConversation;

    public DirectMessage getDm() {
        return dm;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public boolean isNewConversation() {
        return newConversation;
    }

    public String getOtherId() {
        return otherId;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "dm=" + dm +
                ", otherId='" + otherId + '\'' +
                ", messageNumber=" + messageNumber +
                ", newConversation=" + newConversation +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ConversationParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        public Conversation createFromParcel(Parcel source) {
            Conversation target = new Conversation();
            ConversationParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };
}
