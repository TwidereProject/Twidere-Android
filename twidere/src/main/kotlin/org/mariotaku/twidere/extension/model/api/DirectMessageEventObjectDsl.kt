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

import org.mariotaku.microblog.library.model.microblog.DirectMessageEventObject
import org.mariotaku.microblog.library.model.microblog.DirectMessageEventObject.Event
import org.mariotaku.microblog.library.model.microblog.DirectMessageEventObject.Event.MessageCreate

/**
 * Created by mariotaku on 2017/5/11.
 */
fun DirectMessageEventObject(action: Event.() -> Unit): DirectMessageEventObject {
    val obj = DirectMessageEventObject()
    val event = Event()
    action(event)
    obj.event = event
    return obj
}

fun Event.messageCreate(action: MessageCreate.() -> Unit) {
    val messageCreate = MessageCreate()
    action(messageCreate)
    this.messageCreate = messageCreate
}

fun MessageCreate.target(action: MessageCreate.Target.() -> Unit) {
    val target = MessageCreate.Target()
    action(target)
    this.target = target
}

fun MessageCreate.messageData(action: MessageCreate.MessageData.() -> Unit) {
    val messageData = MessageCreate.MessageData()
    action(messageData)
    this.messageData = messageData
}

fun MessageCreate.MessageData.attachment(action: MessageCreate.MessageData.Attachment.() -> Unit) {
    val attachment = MessageCreate.MessageData.Attachment()
    action(attachment)
    this.attachment = attachment
}

fun MessageCreate.MessageData.Attachment.media(action: MessageCreate.MessageData.Attachment.Media.() -> Unit) {
    val media = MessageCreate.MessageData.Attachment.Media()
    action(media)
    this.media = media
}