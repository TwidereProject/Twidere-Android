package org.mariotaku.twidere.extension.model

import android.content.Context
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.model.ParcelableMessage.MessageType
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.ConversationInfoUpdatedExtras
import org.mariotaku.twidere.model.message.MessageExtras
import org.mariotaku.twidere.model.message.UserArrayExtras
import org.mariotaku.twidere.util.UserColorNameManager

val ParcelableMessage.timestamp: Long
    get() = if (message_timestamp > 0) message_timestamp else local_timestamp

fun ParcelableMessage.getSummaryText(context: Context, manager: UserColorNameManager,
        conversation: ParcelableMessageConversation?, nameFirst: Boolean): CharSequence? {
    return getSummaryText(context, manager, nameFirst, message_type, extras, sender_key,
            text_unescaped, conversation)
}

internal fun getSummaryText(context: Context, manager: UserColorNameManager, nameFirst: Boolean,
        messageType: String?, extras: MessageExtras?, senderKey: UserKey?, text: String?,
        conversation: ParcelableMessageConversation?): CharSequence? {
    when (messageType) {
        MessageType.STICKER -> {
            return context.getString(R.string.message_summary_type_sticker)
        }
        MessageType.JOIN_CONVERSATION -> {
            return context.getString(R.string.message_join_conversation)
        }
        MessageType.CONVERSATION_CREATE -> {
            return context.getString(R.string.message_conversation_created)
        }
        MessageType.PARTICIPANTS_JOIN -> {
            val users = (extras as UserArrayExtras).users
            val sender = conversation?.participants?.firstOrNull { senderKey == it.key }
            val res = context.resources
            val joinName = if (users.size == 1) {
                manager.getDisplayName(users[0])
            } else {
                res.getQuantityString(R.plurals.N_users, users.size, users.size)
            }
            if (sender != null) {
                return res.getString(R.string.message_format_participants_join_added,
                        manager.getDisplayName(sender), joinName)
            } else {
                return res.getString(R.string.message_format_participants_join, joinName)
            }
        }
        MessageType.PARTICIPANTS_LEAVE -> {
            val users = (extras as UserArrayExtras).users
            val res = context.resources
            if (users.size == 1) {
                val displayName = manager.getDisplayName(users[0])
                return res.getString(R.string.message_format_participants_leave, displayName)
            } else {
                val usersName = res.getQuantityString(R.plurals.N_users, users.size, users.size)
                return res.getString(R.string.message_format_participants_leave, usersName)
            }
        }
        MessageType.CONVERSATION_NAME_UPDATE -> {
            extras as ConversationInfoUpdatedExtras
            val res = context.resources
            if (extras.user != null) {
                return res.getString(R.string.message_format_conversation_name_update_by_user,
                        manager.getDisplayName(extras.user), extras.name)
            } else {
                return res.getString(R.string.message_format_conversation_name_update, extras.name)
            }
        }
        MessageType.CONVERSATION_AVATAR_UPDATE -> {
            extras as ConversationInfoUpdatedExtras
            val res = context.resources
            if (extras.user != null) {
                return res.getString(R.string.message_format_conversation_avatar_update_by_user,
                        manager.getDisplayName(extras.user))
            } else {
                return res.getString(R.string.message_format_conversation_avatar_update)
            }
        }
    }
    return text
}
