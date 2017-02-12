package org.mariotaku.twidere.model.util

import android.support.annotation.FloatRange
import org.mariotaku.microblog.library.twitter.model.DMResponse
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

    fun fromEntry(accountKey: UserKey, entry: DMResponse.Entry): ParcelableMessage? {
        when {
            entry.message != null -> {
                return ParcelableMessage().apply { applyMessage(accountKey, entry.message) }
            }
            entry.conversationCreate != null -> {
                return ParcelableMessage().apply { applyConversationCreate(accountKey, entry.conversationCreate) }
            }
        }
        return null
    }

    fun incomingMessage(accountKey: UserKey, message: DirectMessage,
            @FloatRange(from = 0.0, to = 1.0) sortIdAdj: Double = 0.0): ParcelableMessage {
        val result = message(accountKey, message, sortIdAdj)
        result.is_outgoing = false
        result.conversation_id = incomingConversationId(message.senderId, message.recipientId)
        return result
    }

    fun outgoingMessage(
            accountKey: UserKey,
            message: DirectMessage,
            @FloatRange(from = 0.0, to = 1.0) sortIdAdj: Double = 0.0
    ): ParcelableMessage {
        val result = message(accountKey, message, sortIdAdj)
        result.is_outgoing = true
        result.conversation_id = outgoingConversationId(message.senderId, message.recipientId)
        return result
    }

    fun incomingConversationId(senderId: String, recipientId: String): String {
        return "$recipientId-$senderId"
    }

    fun outgoingConversationId(senderId: String, recipientId: String): String {
        return "$senderId-$recipientId"
    }

    private fun ParcelableMessage.applyMessage(accountKey: UserKey, message: DMResponse.Entry.Message) {
        this.commonEntry(accountKey, message)

        val data = message.messageData
        this.sender_key = UserKey(data.senderId.toString(), accountKey.host)
        this.recipient_key = UserKey(data.recipientId.toString(), accountKey.host)
        val (text, spans) = InternalTwitterContentUtils.formatDirectMessageText(data)
        this.text_unescaped = text
        this.spans = spans

        this.is_outgoing = this.sender_key == accountKey
    }

    private fun ParcelableMessage.applyConversationCreate(accountKey: UserKey, message: DMResponse.Entry.Message) {
        this.commonEntry(accountKey, message)
        this.message_type = ParcelableMessage.MessageType.CONVERSATION_CREATE
        this.is_outgoing = false
    }

    private fun ParcelableMessage.commonEntry(accountKey: UserKey, message: DMResponse.Entry.Message) {
        this.message_type = ParcelableMessage.MessageType.TEXT
        this.account_key = accountKey
        this.id = message.id.toString()
        this.conversation_id = message.conversationId
        this.message_timestamp = message.time
        this.local_timestamp = this.message_timestamp
        this.sort_id = this.message_timestamp
    }

    private fun message(
            accountKey: UserKey,
            message: DirectMessage,
            @FloatRange(from = 0.0, to = 1.0) sortIdAdj: Double = 0.0
    ): ParcelableMessage {
        val result = ParcelableMessage()
        result.account_key = accountKey
        result.id = message.id
        result.sender_key = UserKeyUtils.fromUser(message.sender)
        result.recipient_key = UserKeyUtils.fromUser(message.recipient)
        result.message_timestamp = message.createdAt.time
        result.local_timestamp = result.message_timestamp
        result.sort_id = result.message_timestamp + (499 * sortIdAdj).toLong()

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
                return Pair(ParcelableMessage.MessageType.STICKER, StickerExtras(singleUrl.expandedUrl))
            }
        }
        return Pair(ParcelableMessage.MessageType.TEXT, null)
    }
}