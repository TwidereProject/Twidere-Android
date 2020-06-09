package org.mariotaku.twidere.extension.model

import android.content.Context
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.twidere.R
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableMessage
import org.mariotaku.twidere.model.ParcelableMessageConversation
import org.mariotaku.twidere.model.ParcelableMessageConversation.ConversationType
import org.mariotaku.twidere.model.ParcelableMessageConversation.ExtrasType
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.message.conversation.DefaultConversationExtras
import org.mariotaku.twidere.model.message.conversation.TwitterOfficialConversationExtras
import org.mariotaku.twidere.util.UserColorNameManager
import java.util.*

fun ParcelableMessageConversation.applyFrom(message: ParcelableMessage, details: AccountDetails) {
    account_key = details.key
    account_color = details.color
    message_type = message.message_type
    message_timestamp = message.message_timestamp
    local_timestamp = message.local_timestamp
    sort_id = message.sort_id
    text_unescaped = message.text_unescaped
    media = message.media
    spans = message.spans
    message_extras = message.extras
    sender_key = message.sender_key
    recipient_key = message.recipient_key
    is_outgoing = message.is_outgoing
    request_cursor = message.request_cursor
}

val ParcelableMessageConversation.timestamp: Long
    get() = if (message_timestamp > 0) message_timestamp else local_timestamp


val ParcelableMessageConversation.user: ParcelableUser?
    get() {
        val userKey = if (is_outgoing) recipient_key else sender_key
        return participants?.firstOrNull { it.key == userKey }
    }

val ParcelableMessageConversation.readOnly: Boolean
    get() {
        when (conversation_extras_type) {
            ExtrasType.TWITTER_OFFICIAL -> {
                return (conversation_extras as? TwitterOfficialConversationExtras)?.readOnly ?: false
            }
        }
        return false
    }

var ParcelableMessageConversation.notificationDisabled: Boolean
    get() {
        return when (conversation_extras_type) {
            ExtrasType.TWITTER_OFFICIAL -> {
                (conversation_extras as? TwitterOfficialConversationExtras)?.notificationsDisabled ?: false
            }
            else -> {
                (conversation_extras as? DefaultConversationExtras)?.notificationsDisabled ?: false
            }
        }
    }
    set(value) {
        when (conversation_extras_type) {
            ExtrasType.TWITTER_OFFICIAL -> {
                val extras = conversation_extras as? TwitterOfficialConversationExtras ?: run {
                    val obj = TwitterOfficialConversationExtras()
                    conversation_extras = obj
                    return@run obj
                }
                extras.notificationsDisabled = value
            }
            else -> {
                val extras = conversation_extras as? DefaultConversationExtras ?: run {
                    val obj = DefaultConversationExtras()
                    conversation_extras = obj
                    return@run obj
                }
                extras.notificationsDisabled = value
            }
        }
    }

fun ParcelableMessageConversation.getTitle(context: Context, manager: UserColorNameManager,
        nameFirst: Boolean): Pair<String, String?> {
    if (conversation_type == ConversationType.ONE_TO_ONE) {
        val user = this.user ?: return Pair(context.getString(R.string.title_direct_messages), null)
        return Pair(user.name, "@${user.screen_name}")
    }
    if (conversation_name != null) {
        return Pair(conversation_name, null)
    }
    return Pair(participants.joinToString(separator = ", ") { manager.getDisplayName(it, nameFirst) }, null)
}

fun ParcelableMessageConversation.getSubtitle(context: Context): String? {
    if (conversation_type == ConversationType.ONE_TO_ONE) {
        val user = this.user ?: return null
        return "@${user.screen_name}"
    }
    val resources = context.resources
    return resources.getQuantityString(R.plurals.N_message_participants, participants.size,
            participants.size)
}

fun ParcelableMessageConversation.getSummaryText(context: Context, manager: UserColorNameManager,
        nameFirst: Boolean): CharSequence? {
    return getSummaryText(context, manager, nameFirst, message_type, message_extras, sender_key,
            text_unescaped, this)
}


fun ParcelableMessageConversation.addParticipants(users: Collection<ParcelableUser>) {
    val participants: Array<ParcelableUser?>? = this.participants
    if (participants == null) {
        if (user != null) {
            this.participants = arrayOf(user)
        } else {
            this.participants = emptyArray()
        }
    } else {
        val addingUsers = ArrayList<ParcelableUser>()
        users.forEach { user ->
            val index = participants.indexOfFirst { it?.key == user.key }
            if (index >= 0) {
                participants[index] = user
            } else {
                addingUsers += user
            }
        }
        this.participants += addingUsers
    }
    this.participant_keys = this.participants.mapToArray(ParcelableUser::key)
    this.participants.sortBy(ParcelableUser::screen_name)
}
