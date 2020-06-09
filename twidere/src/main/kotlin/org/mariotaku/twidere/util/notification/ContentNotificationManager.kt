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

package org.mariotaku.twidere.util.notification

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.forEachRow
import org.mariotaku.ktextension.isEmpty
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.sqliteqb.library.*
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.twidere.BuildConfig
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.HomeActivity
import org.mariotaku.twidere.activity.LinkHandlerActivity
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.annotation.FilterScope
import org.mariotaku.twidere.annotation.NotificationType
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.extension.model.api.formattedTextWithIndices
import org.mariotaku.twidere.extension.queryOne
import org.mariotaku.twidere.extension.queryReference
import org.mariotaku.twidere.extension.rawQuery
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.notification.NotificationChannelSpec
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableActivityUtils
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.receiver.NotificationReceiver
import org.mariotaku.twidere.service.LengthyOperationsService
import org.mariotaku.twidere.util.*
import org.mariotaku.twidere.util.Utils
import org.mariotaku.twidere.util.database.ContentFiltersUtils
import org.oshkimaadziig.george.androidutils.SpanFormatter

class ContentNotificationManager(
        val context: Context,
        val activityTracker: ActivityTracker,
        val userColorNameManager: UserColorNameManager,
        val notificationManager: NotificationManagerWrapper,
        val preferences: SharedPreferences
) {

    private var nameFirst: Boolean = false
    private var useStarForLikes: Boolean = false

    init {
        updatePreferences()
    }

    fun showTimeline(pref: AccountPreferences, minPositionKey: Long) {
        val accountKey = pref.accountKey
        val resources = context.resources
        val selection = Expression.and(Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                Expression.greaterThan(Statuses.POSITION_KEY, minPositionKey),
                Expression.notEquals(Statuses.IS_GAP, 1)
        )
        val selectionArgs = arrayOf(accountKey.toString())
        val filteredSelection = DataStoreUtils.buildStatusFilterWhereClause(preferences, Statuses.TABLE_NAME,
                selection, FilterScope.HOME)
        val userProjection = arrayOf(Statuses.USER_KEY, Statuses.USER_NAME, Statuses.USER_SCREEN_NAME)
        val statusProjection = arrayOf(Statuses.POSITION_KEY)

        @SuppressLint("Recycle")
        val statusCursor = context.contentResolver.query(Statuses.CONTENT_URI, statusProjection,
                filteredSelection.sql, selectionArgs, Statuses.DEFAULT_SORT_ORDER)

        @SuppressLint("Recycle")
        val userCursor = context.contentResolver.rawQuery(SQLQueryBuilder.select(Columns(*userProjection))
                .from(Table(Statuses.TABLE_NAME))
                .where(filteredSelection)
                .groupBy(Column(Statuses.USER_KEY))
                .orderBy(OrderBy(Statuses.DEFAULT_SORT_ORDER)).buildSQL(), selectionArgs)

        try {
            if (statusCursor == null || userCursor == null) return
            val usersCount = userCursor.count
            val statusesCount = statusCursor.count
            if (statusesCount == 0 || usersCount == 0) return
            val statusIndices = ObjectCursor.indicesFrom(statusCursor, ParcelableStatus::class.java)
            val userIndices = ObjectCursor.indicesFrom(userCursor, ParcelableStatus::class.java)
            val positionKey = if (statusCursor.moveToFirst()) {
                statusCursor.getLong(statusIndices[Statuses.POSITION_KEY])
            } else {
                -1L
            }
            val notificationTitle = resources.getQuantityString(R.plurals.N_new_statuses,
                    statusesCount, statusesCount)
            val notificationContent: String
            userCursor.moveToFirst()
            val displayName = userColorNameManager.getDisplayName(userCursor.getString(userIndices[Statuses.USER_KEY]),
                    userCursor.getString(userIndices[Statuses.USER_NAME]), userCursor.getString(userIndices[Statuses.USER_SCREEN_NAME]),
                    nameFirst)
            notificationContent = when (usersCount) {
                1 -> {
                    context.getString(R.string.from_name, displayName)
                }
                2 -> {
                    userCursor.moveToPosition(1)
                    val othersName = userColorNameManager.getDisplayName(userCursor.getString(userIndices[Statuses.USER_KEY]),
                        userCursor.getString(userIndices[Statuses.USER_NAME]), userCursor.getString(userIndices[Statuses.USER_SCREEN_NAME]),
                        nameFirst)
                    resources.getString(R.string.from_name_and_name, displayName, othersName)
                }
                else -> {
                    resources.getString(R.string.from_name_and_N_others, displayName, usersCount - 1)
                }
            }

            // Setup notification
            val builder = NotificationChannelSpec.contentUpdates.accountNotificationBuilder(context,
                    accountKey)
            builder.setAutoCancel(true)
            builder.setSmallIcon(R.drawable.ic_stat_twitter)
            builder.setTicker(notificationTitle)
            builder.setContentTitle(notificationTitle)
            builder.setContentText(notificationContent)
            builder.setCategory(NotificationCompat.CATEGORY_SOCIAL)
            builder.setContentIntent(getContentIntent(context, CustomTabType.HOME_TIMELINE,
                    NotificationType.HOME_TIMELINE, accountKey, positionKey))
            builder.setDeleteIntent(getMarkReadDeleteIntent(context, NotificationType.HOME_TIMELINE,
                    accountKey, positionKey, false))
            builder.setNumber(statusesCount)
            builder.setCategory(NotificationCompat.CATEGORY_SOCIAL)
            applyNotificationPreferences(builder, pref, pref.homeTimelineNotificationType)
            try {
                val notificationId = Utils.getNotificationId(NOTIFICATION_ID_HOME_TIMELINE, accountKey)
                notificationManager.notify("home", notificationId, builder.build())
                Utils.sendPebbleNotification(context, null, notificationContent)
            } catch (e: SecurityException) {
                // Silently ignore
            }

        } finally {
            statusCursor?.close()
            userCursor?.close()
        }
    }

    fun showInteractions(pref: AccountPreferences, position: Long) {
        val am = AccountManager.get(context)
        val cr = context.contentResolver
        val accountKey = pref.accountKey
        val account = AccountUtils.getAccountDetails(am, accountKey, false) ?: return
        val selection = Expression.and(
                Expression.equalsArgs(Activities.ACCOUNT_KEY),
                Expression.greaterThan(Activities.POSITION_KEY, position),
                Expression.notEquals(Activities.IS_GAP, 1)
        )
        val selectionArgs = arrayOf(accountKey.toString())

        val filteredSelection = DataStoreUtils.buildStatusFilterWhereClause(preferences,
                Activities.AboutMe.TABLE_NAME, selection, FilterScope.INTERACTIONS)
        val builder = NotificationChannelSpec.contentInteractions.accountNotificationBuilder(context,
                accountKey)
        val pebbleNotificationStringBuilder = StringBuilder()


        builder.setSmallIcon(R.drawable.ic_stat_notification)
        builder.setCategory(NotificationCompat.CATEGORY_SOCIAL)
        applyNotificationPreferences(builder, pref, pref.mentionsNotificationType)

        val resources = context.resources
        val accountName = userColorNameManager.getDisplayName(account.user, nameFirst)
        builder.setContentText(accountName)
        val style = NotificationCompat.InboxStyle()
        builder.setStyle(style)
        builder.setAutoCancel(true)
        style.setSummaryText(accountName)


        var newMaxPositionKey = -1L
        val filteredUserKeys = DataStoreUtils.getFilteredUserKeys(context, FilterScope.INTERACTIONS)
        val filteredNameKeywords = DataStoreUtils.getFilteredKeywords(context, FilterScope.INTERACTIONS or FilterScope.TARGET_NAME)
        val filteredDescriptionKeywords = DataStoreUtils.getFilteredKeywords(context, FilterScope.INTERACTIONS or FilterScope.TARGET_DESCRIPTION)


        val (remaining, consumed) = cr.queryReference(Activities.AboutMe.CONTENT_URI, Activities.COLUMNS,
                filteredSelection.sql, selectionArgs,
                OrderBy(Activities.TIMESTAMP, false).sql)?.use { (cur) ->
            if (cur.isEmpty) return@use Pair(-1, -1)
            val ci = ObjectCursor.indicesFrom(cur, ParcelableActivity::class.java)
            var con = 0
            val rem = cur.forEachRow(5) { c, _ ->
                val activity = ci.newObject(c)

                if (newMaxPositionKey == -1L) {
                    newMaxPositionKey = activity.position_key
                }

                if (pref.isNotificationMentionsOnly && activity.action !in Activity.Action.MENTION_ACTIONS) {
                    return@forEachRow false
                }
                if (ContentFiltersUtils.isFiltered(cr, activity, true, FilterScope.INTERACTIONS)) {
                    return@forEachRow false
                }
                val sources = ParcelableActivityUtils.filterSources(activity.sources_lite,
                        filteredUserKeys, filteredNameKeywords, filteredDescriptionKeywords,
                        pref.isNotificationFollowingOnly) ?: activity.sources_lite
                        ?: return@forEachRow false

                if (sources.isEmpty()) return@forEachRow false

                val message = ActivityTitleSummaryMessage.get(context, userColorNameManager,
                        activity, sources, 0, useStarForLikes, nameFirst) ?: return@forEachRow false
                val summary = message.summary
                if (summary.isNullOrEmpty()) {
                    style.addLine(message.title)
                    pebbleNotificationStringBuilder.append(message.title)
                    pebbleNotificationStringBuilder.append("\n")
                } else {
                    style.addLine(SpanFormatter.format(resources.getString(R.string.title_summary_line_format),
                            message.title, summary))
                    pebbleNotificationStringBuilder.append(message.title)
                    pebbleNotificationStringBuilder.append(": ")
                    pebbleNotificationStringBuilder.append(summary)
                    pebbleNotificationStringBuilder.append("\n")
                }
                con++
                return@forEachRow true
            }
            return@use Pair(rem, con)
        } ?: Pair(-1, -1)
        if (remaining < 0) return
        if (remaining > 0) {
            style.addLine(resources.getString(R.string.and_N_more, remaining))
            pebbleNotificationStringBuilder.append(resources.getString(R.string.and_N_more, remaining))
        }
        val displayCount = consumed + remaining
        if (displayCount <= 0) return
        val title = resources.getQuantityString(R.plurals.N_new_interactions,
                displayCount, displayCount)
        builder.setContentTitle(title)
        style.setBigContentTitle(title)
        builder.setNumber(displayCount)
        builder.setContentIntent(getContentIntent(context, CustomTabType.NOTIFICATIONS_TIMELINE,
                NotificationType.INTERACTIONS, accountKey, newMaxPositionKey))
        builder.setDeleteIntent(getMarkReadDeleteIntent(context, NotificationType.INTERACTIONS,
                accountKey, newMaxPositionKey, false))

        val notificationId = Utils.getNotificationId(NOTIFICATION_ID_INTERACTIONS_TIMELINE, accountKey)
        notificationManager.notify("interactions", notificationId, builder.build())
        Utils.sendPebbleNotification(context, context.getString(R.string.interactions), pebbleNotificationStringBuilder.toString())
    }

    fun showMessages(pref: AccountPreferences) {
        val resources = context.resources
        val accountKey = pref.accountKey
        val cr = context.contentResolver
        val projection = (Conversations.COLUMNS + Conversations.UNREAD_COUNT).map {
            TwidereQueryBuilder.mapConversationsProjection(it)
        }.toTypedArray()
        val unreadHaving = Expression.greaterThan(Conversations.UNREAD_COUNT, 0)
        cr.getUnreadMessagesEntriesCursorReference(projection, arrayOf(accountKey),
                extraHaving = unreadHaving)?.use { (cur) ->
            if (cur.isEmpty) return

            val indices = ObjectCursor.indicesFrom(cur, ParcelableMessageConversation::class.java)

            var messageSum = 0
            var newLastReadTimestamp = -1L
            cur.forEachRow { c, _ ->
                val unreadCount = c.getInt(indices[Conversations.UNREAD_COUNT])
                if (unreadCount <= 0) return@forEachRow false
                if (newLastReadTimestamp != -1L) {
                    newLastReadTimestamp = c.getLong(indices[Conversations.LAST_READ_TIMESTAMP])
                }
                messageSum += unreadCount
                return@forEachRow true
            }
            if (messageSum == 0) return

            val builder = NotificationChannelSpec.contentMessages.accountNotificationBuilder(context,
                    accountKey)
            applyNotificationPreferences(builder, pref, pref.directMessagesNotificationType)
            builder.setSmallIcon(R.drawable.ic_stat_message)
            builder.setCategory(NotificationCompat.CATEGORY_SOCIAL)
            builder.setAutoCancel(true)
            val style = NotificationCompat.InboxStyle(builder)
            style.setSummaryText(DataStoreUtils.getAccountDisplayName(context, accountKey, nameFirst))

            val notificationTitle = resources.getQuantityString(R.plurals.N_new_messages, messageSum,
                    messageSum)
            builder.setTicker(notificationTitle)
            builder.setContentTitle(notificationTitle)
            builder.setContentIntent(getContentIntent(context, CustomTabType.DIRECT_MESSAGES,
                    NotificationType.DIRECT_MESSAGES, accountKey, 0))
            builder.setDeleteIntent(getMarkReadDeleteIntent(context, NotificationType.DIRECT_MESSAGES,
                    accountKey, newLastReadTimestamp, false))

            val remaining = cur.forEachRow(5) { c, pos ->
                val conversation = indices.newObject(c)
                if (conversation.notificationDisabled) return@forEachRow false
                val title = conversation.getTitle(context, userColorNameManager, nameFirst)
                val summary = conversation.getSummaryText(context, userColorNameManager, nameFirst)
                val line = SpanFormatter.format(context.getString(R.string.title_summary_line_format),
                        title.first, summary)
                if (pos == 0) {
                    builder.setContentText(line)
                }
                style.addLine(line)
                return@forEachRow true
            }
            if (remaining < 0) return
            if (remaining > 0) {
                style.addLine(context.getString(R.string.and_N_more, remaining))
            }
            val notificationId = Utils.getNotificationId(NOTIFICATION_ID_DIRECT_MESSAGES, accountKey)
            notificationManager.notify("direct_messages", notificationId, builder.build())
        }
    }


    fun showUserNotification(accountKey: UserKey, status: Status, userKey: UserKey) {
        // Build favorited user notifications
        val userDisplayName = userColorNameManager.getDisplayName(status.user,
                preferences[nameFirstKey])
        val statusUri = LinkCreator.getTwidereStatusLink(accountKey, status.id)
        val builder = NotificationChannelSpec.contentSubscriptions.accountNotificationBuilder(context,
                accountKey)
        builder.color = userColorNameManager.getUserColor(userKey)
        builder.setAutoCancel(true)
        builder.setWhen(status.createdAt?.time ?: 0)
        builder.setSmallIcon(R.drawable.ic_stat_twitter)
        builder.setCategory(NotificationCompat.CATEGORY_SOCIAL)
        if (status.isRetweetedByMe) {
            builder.setContentTitle(context.getString(R.string.notification_title_new_retweet_by_user, userDisplayName))
            builder.setContentText(status.retweetedStatus.formattedTextWithIndices().text)
        } else {
            builder.setContentTitle(context.getString(R.string.notification_title_new_status_by_user, userDisplayName))
            builder.setContentText(status.formattedTextWithIndices().text)
        }
        builder.setContentIntent(PendingIntent.getActivity(context, 0, Intent(Intent.ACTION_VIEW, statusUri).apply {
            setClass(context, LinkHandlerActivity::class.java)
        }, PendingIntent.FLAG_UPDATE_CURRENT))

        val tag = "$accountKey:$userKey:${status.id}"
        notificationManager.notify(tag, NOTIFICATION_ID_USER_NOTIFICATION, builder.build())
    }

    fun showDraft(draftUri: Uri): Long {
        val draftId = draftUri.lastPathSegment?.toLongOrNull() ?: return -1
        val where = Expression.equals(Drafts._ID, draftId)
        val item = context.contentResolver.queryOne(Drafts.CONTENT_URI, Drafts.COLUMNS, where.sql,
                null, null, Draft::class.java) ?: return -1
        val title = context.getString(R.string.status_not_updated)
        val message = context.getString(R.string.status_not_updated_summary)
        val intent = Intent()
        intent.`package` = BuildConfig.APPLICATION_ID
        val uriBuilder = Uri.Builder()
        uriBuilder.scheme(SCHEME_TWIDERE)
        uriBuilder.authority(AUTHORITY_DRAFTS)
        intent.data = uriBuilder.build()
        val nb = NotificationChannelSpec.appNotices.notificationBuilder(context)
        nb.setTicker(message)
        nb.setContentTitle(title)
        nb.setContentText(item.text)
        nb.setAutoCancel(true)
        nb.setWhen(System.currentTimeMillis())
        nb.setSmallIcon(R.drawable.ic_stat_draft)
        val discardIntent = Intent(context, LengthyOperationsService::class.java)
        discardIntent.action = INTENT_ACTION_DISCARD_DRAFT
        discardIntent.data = draftUri
        nb.addAction(R.drawable.ic_action_delete, context.getString(R.string.discard),
                PendingIntent.getService(context, 0, discardIntent, PendingIntent.FLAG_ONE_SHOT))

        val sendIntent = Intent(context, LengthyOperationsService::class.java)
        sendIntent.action = INTENT_ACTION_SEND_DRAFT
        sendIntent.data = draftUri
        nb.addAction(R.drawable.ic_action_send, context.getString(R.string.action_send),
                PendingIntent.getService(context, 0, sendIntent, PendingIntent.FLAG_ONE_SHOT))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        nb.setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT))
        notificationManager.notify(draftUri.toString(), NOTIFICATION_ID_DRAFTS, nb.build())
        return draftId
    }

    fun updatePreferences() {
        nameFirst = preferences[nameFirstKey]
        useStarForLikes = preferences[iWantMyStarsBackKey]
    }

    private fun applyNotificationPreferences(builder: NotificationCompat.Builder, pref: AccountPreferences, defaultFlags: Int) {
        var notificationDefaults = 0
        if (AccountPreferences.isNotificationHasLight(defaultFlags)) {
            notificationDefaults = notificationDefaults or NotificationCompat.DEFAULT_LIGHTS
        }
        if (isNotificationAudible()) {
            notificationDefaults = if (AccountPreferences.isNotificationHasVibration(defaultFlags)) {
                notificationDefaults or NotificationCompat.DEFAULT_VIBRATE
            } else {
                notificationDefaults and NotificationCompat.DEFAULT_VIBRATE.inv()
            }
            if (AccountPreferences.isNotificationHasRingtone(defaultFlags)) {
                builder.setSound(pref.notificationRingtone, AudioManager.STREAM_NOTIFICATION)
            }
        } else {
            notificationDefaults = notificationDefaults and (NotificationCompat.DEFAULT_VIBRATE or NotificationCompat.DEFAULT_SOUND).inv()
        }
        builder.color = pref.notificationLightColor
        builder.setDefaults(notificationDefaults)
        builder.setOnlyAlertOnce(true)
    }

    private fun isNotificationAudible(): Boolean {
        return !activityTracker.isHomeActivityStarted
    }

    private fun getContentIntent(context: Context, @CustomTabType type: String,
            @NotificationType notificationType: String, accountKey: UserKey?, readPosition: Long): PendingIntent {
        // Setup click intent
        val homeIntent = Intent(context, HomeActivity::class.java)
        val homeLinkBuilder = Uri.Builder()
        homeLinkBuilder.scheme(SCHEME_TWIDERE)
        homeLinkBuilder.authority(type)
        if (accountKey != null)
            homeLinkBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        homeLinkBuilder.appendQueryParameter(QUERY_PARAM_FROM_NOTIFICATION, true.toString())
        homeLinkBuilder.appendQueryParameter(QUERY_PARAM_TIMESTAMP, System.currentTimeMillis().toString())
        homeLinkBuilder.appendQueryParameter(QUERY_PARAM_NOTIFICATION_TYPE, notificationType)
        if (readPosition > 0) {
            homeLinkBuilder.appendQueryParameter(QUERY_PARAM_READ_POSITION, readPosition.toString())
        }
        homeIntent.data = homeLinkBuilder.build()
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return PendingIntent.getActivity(context, 0, homeIntent, 0)
    }

    private fun getMarkReadDeleteIntent(context: Context, @NotificationType type: String,
            accountKey: UserKey?, position: Long,
            extraUserFollowing: Boolean): PendingIntent {
        return getMarkReadDeleteIntent(context, type, accountKey, position)
    }

    private fun getMarkReadDeleteIntent(context: Context, @NotificationType type: String,
            accountKey: UserKey?, position: Long): PendingIntent {
        // Setup delete intent
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.action = IntentConstants.BROADCAST_NOTIFICATION_DELETED
        val linkBuilder = Uri.Builder()
        linkBuilder.scheme(SCHEME_TWIDERE)
        linkBuilder.authority(AUTHORITY_NOTIFICATIONS)
        linkBuilder.appendPath(type)
        if (accountKey != null) {
            linkBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        linkBuilder.appendQueryParameter(QUERY_PARAM_READ_POSITION, position.toString())
        linkBuilder.appendQueryParameter(QUERY_PARAM_NOTIFICATION_TYPE, type)

        intent.data = linkBuilder.build()
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }
}