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

package org.mariotaku.microblog.library.twitter.model

import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.DMResponse.Entry.Message

/**
 * I don't know why Twitter doesn't return video/animatedGif when requesting DM, but this will help
 */
fun DMResponse.fixMedia(microBlog: MicroBlog) {
    entries?.forEach { entry ->
        // Ensure it's a normal message
        val data = entry.message?.messageData ?: return@forEach
        // Ensure this message don't have attachment
        if (data.attachment != null) return@forEach

        // Don't try if it's a group dm
        if (conversations?.get(entry.message.conversationId)?.type == DMResponse.Conversation.Type.GROUP_DM) {
            return@forEach
        }

        val mediaUrl = "https://twitter.com/messages/media/${data.id}"
        if (data.entities?.urls?.find { it.expandedUrl == mediaUrl } == null) return@forEach
        val message = try {
            microBlog.showDirectMessage(data.id)
        } catch (e: MicroBlogException) {
            // Ignore
            return@forEach
        }
        val media = message.entities?.media?.find { it.expandedUrl == mediaUrl } ?: return@forEach

        when (media.type) {
            MediaEntity.Type.VIDEO -> {
                data.attachment = attachment { video = media }
            }
            MediaEntity.Type.PHOTO -> {
                data.attachment = attachment { photo = media }
            }
            MediaEntity.Type.ANIMATED_GIF -> {
                data.attachment = attachment { animatedGif = media }
            }
        }
    }
}

private inline fun attachment(apply: Message.Data.Attachment.() -> Unit): Message.Data.Attachment {
    val attachment = Message.Data.Attachment()
    apply(attachment)
    return attachment
}