package org.mariotaku.twidere.extension.model

import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.model.ParcelableMessageConversation

fun ParcelableMessageConversation.setFrom(message: ParcelableMessage) {
    account_key = message.account_key
    id = message.conversation_id
    message_type = message.message_type
    message_timestamp = message.message_timestamp
    local_timestamp = message.local_timestamp
    text_unescaped = message.text_unescaped
    media = message.media
    spans = message.spans
    extras = message.extras
    sender_key = message.sender_key
    recipient_key = message.recipient_key
    is_outgoing = message.is_outgoing
    request_cursor = message.request_cursor
}

val ParcelableMessageConversation.timestamp: Long
    get() = if (message_timestamp > 0) message_timestamp else local_timestamp
