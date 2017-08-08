/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.extension.model.api

import org.mariotaku.microblog.library.twitter.model.DirectMessageEventObject

/**
 * Created by mariotaku on 2017/5/11.
 */
fun DirectMessageEventObject(action: DirectMessageEventObject.Event.() -> Unit): DirectMessageEventObject {
    val obj = DirectMessageEventObject()
    val event = DirectMessageEventObject.Event()
    action(event)
    obj.event = event
    return obj
}

fun DirectMessageEventObject.Event.messageCreate(action: DirectMessageEventObject.Event.MessageCreate.() -> Unit) {
    val messageCreate = DirectMessageEventObject.Event.MessageCreate()
    action(messageCreate)
    this.messageCreate = messageCreate
}

fun DirectMessageEventObject.Event.MessageCreate.target(action: DirectMessageEventObject.Event.MessageCreate.Target.() -> Unit) {
    val target = DirectMessageEventObject.Event.MessageCreate.Target()
    action(target)
    this.target = target
}

fun DirectMessageEventObject.Event.MessageCreate.messageData(action: DirectMessageEventObject.Event.MessageCreate.MessageData.() -> Unit) {
    val messageData = DirectMessageEventObject.Event.MessageCreate.MessageData()
    action(messageData)
    this.messageData = messageData
}

fun DirectMessageEventObject.Event.MessageCreate.MessageData.attachment(action: DirectMessageEventObject.Event.MessageCreate.MessageData.Attachment.() -> Unit) {
    val attachment = DirectMessageEventObject.Event.MessageCreate.MessageData.Attachment()
    action(attachment)
    this.attachment = attachment
}

fun DirectMessageEventObject.Event.MessageCreate.MessageData.Attachment.media(action: DirectMessageEventObject.Event.MessageCreate.MessageData.Attachment.Media.() -> Unit) {
    val media = DirectMessageEventObject.Event.MessageCreate.MessageData.Attachment.Media()
    action(media)
    this.media = media
}