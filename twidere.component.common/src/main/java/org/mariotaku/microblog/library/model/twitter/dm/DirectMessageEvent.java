/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2018 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.microblog.library.model.twitter.dm;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.microblog.library.model.microblog.Entities;

@JsonObject
public class DirectMessageEvent {
    @JsonField(name = "id")
    String id;

    @JsonField(name = "created_timestamp")
    long created_timestamp;

    @JsonField(name = "type")
    String type;

    @JsonField(name = "message_create")
    MessageCreate messageCreate;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public MessageCreate getMessageCreate() {
        return messageCreate;
    }

    public void setMessageCreate(MessageCreate messageCreate) {
        this.messageCreate = messageCreate;
    }


    public String getId() {
        return id;
    }


    @JsonObject
    public static class MessageCreate {

        @JsonField(name = "sender_id")
        String senderId;
        @JsonField(name = "source_app_id")
        String sourceAppId;
        @JsonField(name = "target")
        MessageCreate.Target target;
        @JsonField(name = "message_data")
        MessageCreate.MessageData messageData;

        public String getSenderId() {
            return senderId;
        }

        public String getSourceAppId() {
            return sourceAppId;
        }

        public MessageCreate.Target getTarget() {
            return target;
        }

        public void setTarget(MessageCreate.Target target) {
            this.target = target;
        }

        public MessageCreate.MessageData getMessageData() {
            return messageData;
        }

        public void setMessageData(MessageCreate.MessageData messageData) {
            this.messageData = messageData;
        }

        @JsonObject
        public static class Target {
            @JsonField(name = "recipient_id")
            String recipientId;

            public String getRecipientId() {
                return recipientId;
            }

            public void setRecipientId(String recipientId) {
                this.recipientId = recipientId;
            }
        }

        @JsonObject
        public static class MessageData {
            @JsonField(name = "text")
            String text;
            @JsonField(name = "attachment")
            Attachment attachment;
            @JsonField(name = "attachment")
            Entities entities;

            public String getText() {
                return text;
            }

            public void setText(String text) {
                this.text = text;
            }

            public Attachment getAttachment() {
                return attachment;
            }

            public void setAttachment(Attachment attachment) {
                this.attachment = attachment;
            }

        }

    }

}
