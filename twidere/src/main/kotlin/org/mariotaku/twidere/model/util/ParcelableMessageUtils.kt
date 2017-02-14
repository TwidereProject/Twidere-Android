package org.mariotaku.twidere.model.util

import android.support.annotation.FloatRange
import org.mariotaku.ktextension.convert
import org.mariotaku.microblog.library.twitter.model.DMResponse
import org.mariotaku.microblog.library.twitter.model.DMResponse.Entry.Message
import org.mariotaku.microblog.library.twitter.model.DMResponse.Entry.Message.Data
import org.mariotaku.microblog.library.twitter.model.DirectMessage
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.model.ParcelableMedia
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.model.ParcelableMessage.MessageType
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.ConversationInfoUpdatedExtras
import org.mariotaku.twidere.model.message.MessageExtras
import org.mariotaku.twidere.model.message.StickerExtras
import org.mariotaku.twidere.model.message.UserArrayExtras
import org.mariotaku.twidere.util.InternalTwitterContentUtils

/**
 * Created by mariotaku on 2017/2/9.
 */
object ParcelableMessageUtils {

    fun fromMessage(accountKey: UserKey, message: DirectMessage, outgoing: Boolean,
            @FloatRange(from = 0.0, to = 1.0) sortIdAdj: Double = 0.0): ParcelableMessage {
        val result = ParcelableMessage()
        result.applyMessage(accountKey, message, sortIdAdj)
        result.is_outgoing = outgoing
        if (outgoing) {
            result.conversation_id = outgoingConversationId(message.senderId, message.recipientId)
        } else {
            result.conversation_id = incomingConversationId(message.senderId, message.recipientId)
        }
        return result
    }

    fun fromEntry(accountKey: UserKey, entry: DMResponse.Entry, users: Map<String, User>): ParcelableMessage? {
        when {
            entry.message != null -> {
                return ParcelableMessage().apply { applyMessage(accountKey, entry.message) }
            }
            entry.conversationCreate != null -> {
                return ParcelableMessage().apply { applyConversationCreate(accountKey, entry.conversationCreate) }
            }
            entry.joinConversation != null -> {
                return ParcelableMessage().apply {
                    applyUsersEvent(accountKey, entry.joinConversation, users, MessageType.JOIN_CONVERSATION)
                }
            }
            entry.participantsLeave != null -> {
                return ParcelableMessage().apply {
                    applyUsersEvent(accountKey, entry.participantsLeave, users, MessageType.PARTICIPANTS_LEAVE)
                }
            }
            entry.participantsJoin != null -> {
                return ParcelableMessage().apply {
                    applyUsersEvent(accountKey, entry.participantsJoin, users, MessageType.PARTICIPANTS_JOIN)
                }
            }
            entry.conversationNameUpdate != null -> {
                return ParcelableMessage().apply {
                    applyInfoUpdatedEvent(accountKey, entry.conversationNameUpdate, users,
                            MessageType.CONVERSATION_NAME_UPDATE)
                }
            }
            entry.conversationAvatarUpdate != null -> {
                return ParcelableMessage().apply {
                    applyInfoUpdatedEvent(accountKey, entry.conversationAvatarUpdate, users,
                            MessageType.CONVERSATION_AVATAR_UPDATE)
                }
            }
        }
        return null
    }

    fun incomingConversationId(senderId: String, recipientId: String): String {
        return "$recipientId-$senderId"
    }

    fun outgoingConversationId(senderId: String, recipientId: String): String {
        return "$senderId-$recipientId"
    }

    private fun ParcelableMessage.applyMessage(accountKey: UserKey, message: Message) {
        this.commonEntry(accountKey, message)

        val data = message.messageData
        val (type, extras, media) = typeAndExtras(data)
        val (text, spans) = InternalTwitterContentUtils.formatDirectMessageText(data)
        this.message_type = type
        this.text_unescaped = text
        this.extras = extras
        this.spans = spans
        this.media = media
    }

    private fun ParcelableMessage.applyConversationCreate(accountKey: UserKey, message: Message) {
        this.commonEntry(accountKey, message)
        this.message_type = MessageType.CONVERSATION_CREATE
        this.is_outgoing = false
    }

    private fun ParcelableMessage.applyUsersEvent(accountKey: UserKey, message: Message,
            users: Map<String, User>, @MessageType type: String) {
        this.commonEntry(accountKey, message)
        this.message_type = type
        this.extras = UserArrayExtras().apply {
            this.users = message.participants.mapNotNull {
                val user = users[it.userId] ?: return@mapNotNull null
                ParcelableUserUtils.fromUser(user, accountKey)
            }.toTypedArray()
        }
        this.is_outgoing = false
    }

    private fun ParcelableMessage.applyInfoUpdatedEvent(accountKey: UserKey, message: Message,
            users: Map<String, User>, @MessageType type: String) {
        this.commonEntry(accountKey, message)
        this.message_type = type
        this.extras = ConversationInfoUpdatedExtras().apply {
            this.name = message.conversationName
            this.avatar = message.conversationAvatarImageHttps
            this.user = users[message.byUserId]?.convert { ParcelableUserUtils.fromUser(it, accountKey) }
        }
        this.is_outgoing = false
    }

    private fun ParcelableMessage.commonEntry(accountKey: UserKey, message: Message) {
        val data = message.messageData
        this.sender_key = run {
            val senderId = data?.senderId ?: message.senderId ?: return@run null
            return@run UserKey(senderId, accountKey.host)
        }
        this.recipient_key = run {
            val recipientId = data?.recipientId ?: return@run null
            return@run UserKey(recipientId, accountKey.host)
        }
        this.account_key = accountKey
        this.id = message.id.toString()
        this.conversation_id = message.conversationId
        this.message_timestamp = message.time
        this.local_timestamp = this.message_timestamp
        this.sort_id = this.message_timestamp

        this.is_outgoing = this.sender_key == accountKey
    }

    private fun ParcelableMessage.applyMessage(
            accountKey: UserKey,
            message: DirectMessage,
            @FloatRange(from = 0.0, to = 1.0) sortIdAdj: Double = 0.0
    ) {
        this.account_key = accountKey
        this.id = message.id
        this.sender_key = UserKeyUtils.fromUser(message.sender)
        this.recipient_key = UserKeyUtils.fromUser(message.recipient)
        this.message_timestamp = message.createdAt.time
        this.local_timestamp = this.message_timestamp
        this.sort_id = this.message_timestamp + (499 * sortIdAdj).toLong()

        val (type, extras) = typeAndExtras(message)
        val (text, spans) = InternalTwitterContentUtils.formatDirectMessageText(message)
        this.message_type = type
        this.extras = extras
        this.text_unescaped = text
        this.spans = spans
        this.media = ParcelableMediaUtils.fromEntities(message)
    }

    private fun typeAndExtras(message: DirectMessage): Pair<String, MessageExtras?> {
        val singleUrl = message.urlEntities?.singleOrNull()
        if (singleUrl != null) {
            if (singleUrl.expandedUrl.startsWith("https://twitter.com/i/stickers/image/")) {
                return Pair(MessageType.STICKER, StickerExtras(singleUrl.expandedUrl))
            }
        }
        return Pair(MessageType.TEXT, null)
    }

    private fun typeAndExtras(data: Data): Triple<String, MessageExtras?, Array<ParcelableMedia>?> {
        val attachment = data.attachment ?: return Triple(MessageType.TEXT, null, null)
        when {
            attachment.photo != null -> {
                val photo = attachment.photo
                val media = arrayOf(ParcelableMediaUtils.fromMediaEntity(photo))
                return Triple(MessageType.TEXT, null, media)
            }
            attachment.video != null -> {
                val video = attachment.video
                val media = arrayOf(ParcelableMediaUtils.fromMediaEntity(video))
                return Triple(MessageType.TEXT, null, media)
            }
            attachment.sticker != null -> {
                val sticker = attachment.sticker
                val image = sticker.images["size_2x"] ?: sticker.images.values.firstOrNull() ?:
                        return Triple(MessageType.TEXT, null, null)
                val extras = StickerExtras(image.url)
                extras.displayName = sticker.displayName
                return Triple(MessageType.STICKER, extras, null)
            }
        }
        return Triple(MessageType.TEXT, null, null)
    }
}