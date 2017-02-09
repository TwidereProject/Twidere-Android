package org.mariotaku.twidere.model.util

import org.mariotaku.microblog.library.twitter.model.DirectMessage
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.MessageExtras
import org.mariotaku.twidere.model.message.StickerExtras
import org.mariotaku.twidere.util.InternalTwitterContentUtils

/**
 * Created by mariotaku on 2017/2/9.
 */
object ParcelableMessageUtils {
    fun incomingMessage(accountKey: UserKey, message: DirectMessage): ParcelableMessage {
        val result = message(accountKey, message)
        result.is_outgoing = false
        result.conversation_id = "${message.recipientId}-${message.senderId}"
        return result
    }

    fun outgoingMessage(accountKey: UserKey, message: DirectMessage): ParcelableMessage {
        val result = message(accountKey, message)
        result.is_outgoing = true
        result.conversation_id = "${message.senderId}-${message.recipientId}"
        return result
    }

    private fun message(accountKey: UserKey, message: DirectMessage): ParcelableMessage {
        val result = ParcelableMessage()
        result.account_key = accountKey
        result.id = message.id
        result.sender_key = UserKeyUtils.fromUser(message.sender)
        result.recipient_key = UserKeyUtils.fromUser(message.recipient)
        result.message_timestamp = message.createdAt.time
        result.local_timestamp = result.message_timestamp

        val (type, extras) = typeAndExtras(accountKey, message)
        val (text, spans) = InternalTwitterContentUtils.formatDirectMessageText(message)
        result.message_type = type
        result.extras = extras
        result.text_unescaped = text
        result.spans = spans
        result.media = ParcelableMediaUtils.fromEntities(message)
        return result
    }

    private fun typeAndExtras(accountKey: UserKey, message: DirectMessage): Pair<String, MessageExtras?> {
        val singleUrl = message.urlEntities?.singleOrNull()
        if (singleUrl != null) {
            if (singleUrl.expandedUrl.startsWith("https://twitter.com/i/stickers/image/")) {
                return Pair(ParcelableMessage.Type.STICKER, StickerExtras(singleUrl.expandedUrl))
            }
        }
        return Pair(ParcelableMessage.Type.TEXT, null)
    }
}