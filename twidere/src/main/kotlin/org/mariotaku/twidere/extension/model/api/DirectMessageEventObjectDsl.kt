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

import org.mariotaku.microblog.library.model.twitter.dm.DirectMessageEvent
import org.mariotaku.microblog.library.model.twitter.dm.DirectMessageEvent.MessageCreate
import org.mariotaku.microblog.library.model.twitter.dm.DirectMessageEventObject
import org.mariotaku.microblog.library.model.twitter.dm.Attachment

fun DirectMessageEventObject(action: DirectMessageEvent.() -> Unit): DirectMessageEventObject {
    val obj = DirectMessageEventObject()
    val event = DirectMessageEvent()
    action(event)
    obj.event = event
    return obj
}

fun DirectMessageEvent.messageCreate(action: MessageCreate.() -> Unit) {
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

fun MessageCreate.MessageData.attachment(action: Attachment.() -> Unit) {
    val attachment = Attachment()
    action(attachment)
    this.attachment = attachment
}

fun Attachment.media(action: Attachment.Media.() -> Unit) {
    val media = Attachment.Media()
    action(media)
    this.media = media
}