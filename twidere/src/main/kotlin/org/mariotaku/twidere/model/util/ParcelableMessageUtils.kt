package org.mariotaku.twidere.model.util

import org.mariotaku.microblog.library.twitter.model.DirectMessage
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.model.UserKey
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

        val (text, spans) = InternalTwitterContentUtils.formatDirectMessageText(message)
        result.text_unescaped = text
        result.spans = spans
        result.media = ParcelableMediaUtils.fromEntities(message)
        return result
    }
}