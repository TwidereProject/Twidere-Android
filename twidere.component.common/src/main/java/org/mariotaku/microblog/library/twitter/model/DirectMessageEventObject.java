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

package org.mariotaku.microblog.library.twitter.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by mariotaku on 2017/5/11.
 */
@JsonObject
public class DirectMessageEventObject {
    @JsonField(name = "event")
    Event event;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @JsonObject
    public static class Event {
        @JsonField(name = "id")
        String id;

        @JsonField(name = "type")
        String type;

        @JsonField(name = "message_create")
        MessageCreate messageCreate;

        @JsonField(name = "created_timestamp")
        String createdTimestamp;

        public String getCreatedTimestamp() {
            return createdTimestamp;
        }

        public void setCreatedTimestamp(String createdTimestamp) {
            this.createdTimestamp = createdTimestamp;
        }

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

            @JsonField(name = "target")
            Target target;
            @JsonField(name = "message_data")
            MessageData messageData;
            @JsonField(name = "sender_id")
            String senderId;

            public String getSenderId() {
                return senderId;
            }

            public void setSenderId(String senderId) {
                this.senderId = senderId;
            }

            public Target getTarget() {
                return target;
            }

            public void setTarget(Target target) {
                this.target = target;
            }

            public MessageData getMessageData() {
                return messageData;
            }

            public void setMessageData(MessageData messageData) {
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

                @JsonObject
                public static class Attachment {
                    @JsonField(name = "type")
                    String type;

                    @JsonField(name = "media")
                    Media media;

                    public String getType() {
                        return type;
                    }

                    public void setType(String type) {
                        this.type = type;
                    }

                    public Media getMedia() {
                        return media;
                    }

                    public void setMedia(Media media) {
                        this.media = media;
                    }

                    @JsonObject
                    public static class Media {
                        @JsonField(name = "id")
                        String id;

                        public String getId() {
                            return id;
                        }

                        public void setId(String id) {
                            this.id = id;
                        }
                    }
                }
            }

        }
    }
}
