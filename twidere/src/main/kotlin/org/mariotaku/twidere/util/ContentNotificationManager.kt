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

package org.mariotaku.twidere.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.media.AudioManager
import android.net.Uri
import android.support.v4.app.NotificationCompat
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.isEmpty
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.sqliteqb.library.*
import org.mariotaku.sqliteqb.library.Columns.Column
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.*
import org.mariotaku.twidere.activity.HomeActivity
import org.mariotaku.twidere.annotation.CustomTabType
import org.mariotaku.twidere.annotation.NotificationType
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.iWantMyStarsBackKey
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.model.getConversationName
import org.mariotaku.twidere.extension.model.getSummaryText
import org.mariotaku.twidere.extension.model.notificationDisabled
import org.mariotaku.twidere.extension.rawQuery
import org.mariotaku.twidere.model.*
import org.mariotaku.twidere.model.util.ParcelableActivityUtils
import org.mariotaku.twidere.provider.TwidereDataStore.*
import org.mariotaku.twidere.provider.TwidereDataStore.Messages.Conversations
import org.mariotaku.twidere.receiver.NotificationReceiver
import org.mariotaku.twidere.util.database.FilterQueryBuilder
import org.oshkimaadziig.george.androidutils.SpanFormatter
import java.io.IOException

/**
 * Created by mariotaku on 2017/2/16.
 */
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
                Expression.greaterThan(Statuses.POSITION_KEY, minPositionKey))
        val filteredSelection = buildStatusFilterWhereClause(preferences, Statuses.TABLE_NAME,
                selection)
        val selectionArgs = arrayOf(accountKey.toString())
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
            val statusIndices = ParcelableStatusCursorIndices(statusCursor)
            val userIndices = ParcelableStatusCursorIndices(userCursor)
            val positionKey = if (statusCursor.moveToFirst()) statusCursor.getLong(statusIndices.position_key) else -1L
            val notificationTitle = resources.getQuantityString(R.plurals.N_new_statuses,
                    statusesCount, statusesCount)
            val notificationContent: String
            userCursor.moveToFirst()
            val displayName = userColorNameManager.getDisplayName(userCursor.getString(userIndices.user_key),
                    userCursor.getString(userIndices.user_name), userCursor.getString(userIndices.user_screen_name),
                    nameFirst)
            if (usersCount == 1) {
                notificationContent = context.getString(R.string.from_name, displayName)
            } else if (usersCount == 2) {
                userCursor.moveToPosition(1)
                val othersName = userColorNameManager.getDisplayName(userCursor.getString(userIndices.user_key),
                        userCursor.getString(userIndices.user_name), userCursor.getString(userIndices.user_screen_name),
                        nameFirst)
                notificationContent = resources.getString(R.string.from_name_and_name, displayName, othersName)
            } else {
                notificationContent = resources.getString(R.string.from_name_and_N_others, displayName, usersCount - 1)
            }

            // Setup notification
            val builder = NotificationCompat.Builder(context)
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
        val cr = context.contentResolver
        val accountKey = pref.accountKey
        val where = Expression.and(
                Expression.equalsArgs(Activities.ACCOUNT_KEY),
                Expression.greaterThanArgs(Activities.POSITION_KEY)
        ).sql
        val whereArgs = arrayOf(accountKey.toString(), position.toString())
        @SuppressLint("Recycle")
        val c = cr.query(Activities.AboutMe.CONTENT_URI, Activities.COLUMNS, where, whereArgs,
                OrderBy(Activities.TIMESTAMP, false).sql) ?: return
        val builder = NotificationCompat.Builder(context)
        val pebbleNotificationStringBuilder = StringBuilder()
        try {
            val count = c.count
            if (count == 0) return
            builder.setSmallIcon(R.drawable.ic_stat_notification)
            builder.setCategory(NotificationCompat.CATEGORY_SOCIAL)
            applyNotificationPreferences(builder, pref, pref.mentionsNotificationType)

            val resources = context.resources
            val accountName = DataStoreUtils.getAccountDisplayName(context, accountKey, nameFirst)
            builder.setContentText(accountName)
            val style = NotificationCompat.InboxStyle()
            builder.setStyle(style)
            builder.setAutoCancel(true)
            style.setSummaryText(accountName)
            val ci = ParcelableActivityCursorIndices(c)

            var timestamp: Long = -1
            val filteredUserIds = DataStoreUtils.getFilteredUserIds(context)
            val remaining = c.forEachRow(5) { cur, idx ->

                val activity = ci.newObject(c)
                if (pref.isNotificationMentionsOnly && activity.action !in Activity.Action.MENTION_ACTIONS) {
                    return@forEachRow false
                }
                if (activity.status_id != null && FilterQueryBuilder.isFiltered(cr, activity)) {
                    return@forEachRow false
                }
                ParcelableActivityUtils.initAfterFilteredSourceIds(activity, filteredUserIds,
                        pref.isNotificationFollowingOnly)
                val sources = ParcelableActivityUtils.getAfterFilteredSources(activity)

                if (sources.isEmpty()) return@forEachRow false


                if (timestamp == -1L) {
                    timestamp = activity.timestamp
                }

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
                return@forEachRow true
            }
            if (remaining < 0) return
            if (remaining > 0) {
                style.addLine(resources.getString(R.string.and_N_more, count - c.position))
                pebbleNotificationStringBuilder.append(resources.getString(R.string.and_N_more, count - c.position))
            }
            val displayCount = 5 + remaining
            val title = resources.getQuantityString(R.plurals.N_new_interactions,
                    displayCount, displayCount)
            builder.setContentTitle(title)
            style.setBigContentTitle(title)
            builder.setNumber(displayCount)
            builder.setContentIntent(getContentIntent(context, CustomTabType.NOTIFICATIONS_TIMELINE,
                    NotificationType.INTERACTIONS, accountKey, timestamp))
            if (timestamp != -1L) {
                builder.setDeleteIntent(getMarkReadDeleteIntent(context,
                        NotificationType.INTERACTIONS, accountKey, timestamp, false))
            }
        } catch (e: IOException) {
            return
        } finally {
            c.close()
        }
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
        val qb = SQLQueryBuilder.select(Columns(*projection))
        qb.from(Table(Conversations.TABLE_NAME))
        qb.join(Join(false, Join.Operation.LEFT_OUTER, Table(Messages.TABLE_NAME),
                Expression.equals(
                        Column(Table(Conversations.TABLE_NAME), Conversations.CONVERSATION_ID),
                        Column(Table(Messages.TABLE_NAME), Messages.CONVERSATION_ID)
                )
        ))
        qb.where(Expression.and(
                Expression.equalsArgs(Column(Table(Conversations.TABLE_NAME), Conversations.ACCOUNT_KEY)),
                Expression.lesserThan(Column(Table(Conversations.TABLE_NAME), Conversations.LAST_READ_TIMESTAMP),
                        Column(Table(Conversations.TABLE_NAME), Conversations.LOCAL_TIMESTAMP))
        ))
        qb.groupBy(Column(Table(Messages.TABLE_NAME), Messages.CONVERSATION_ID))
        qb.orderBy(OrderBy(arrayOf(Conversations.LOCAL_TIMESTAMP, Conversations.SORT_ID), booleanArrayOf(false, false)))
        val selectionArgs = arrayOf(accountKey.toString())
        val cur = cr.rawQuery(qb.buildSQL(), selectionArgs) ?: return
        try {
            if (cur.isEmpty) return

            val indices = ParcelableMessageConversationCursorIndices(cur)

            var messageSum: Int = 0
            cur.forEachRow { cur, pos ->
                messageSum += cur.getInt(indices.unread_count)
                return@forEachRow true
            }

            val builder = NotificationCompat.Builder(context)
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
            val remaining = cur.forEachRow(5) { cur, pos ->
                val conversation = indices.newObject(cur)
                if (conversation.notificationDisabled) return@forEachRow false
                val title = conversation.getConversationName(context, userColorNameManager, nameFirst)
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
        } finally {
            cur.close()
        }
    }

    /**
     * @param limit -1 for no limit
     * @return Remaining count, -1 if no rows present
     */
    private inline fun Cursor.forEachRow(limit: Int = -1, action: (cur: Cursor, pos: Int) -> Boolean): Int {
        moveToFirst()
        var current = 0
        while (!isAfterLast) {
            if (limit >= 0 && current >= limit) break
            if (action(this, current)) {
                current++
            }
            moveToNext()
        }
        return count - position
    }

    private fun applyNotificationPreferences(builder: NotificationCompat.Builder, pref: AccountPreferences, defaultFlags: Int) {
        var notificationDefaults = 0
        if (AccountPreferences.isNotificationHasLight(defaultFlags)) {
            notificationDefaults = notificationDefaults or NotificationCompat.DEFAULT_LIGHTS
        }
        if (isNotificationAudible()) {
            if (AccountPreferences.isNotificationHasVibration(defaultFlags)) {
                notificationDefaults = notificationDefaults or NotificationCompat.DEFAULT_VIBRATE
            } else {
                notificationDefaults = notificationDefaults and NotificationCompat.DEFAULT_VIBRATE.inv()
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

    fun updatePreferences() {
        nameFirst = preferences[nameFirstKey]
        useStarForLikes = preferences[iWantMyStarsBackKey]
    }

    private fun getMarkReadDeleteIntent(context: Context, @NotificationType type: String,
            accountKey: UserKey?, position: Long,
            extraUserFollowing: Boolean): PendingIntent {
        return getMarkReadDeleteIntent(context, type, accountKey, position, -1, -1, extraUserFollowing)
    }

    private fun getMarkReadDeleteIntent(context: Context, @NotificationType type: String,
            accountKey: UserKey?, position: Long,
            extraId: Long, extraUserId: Long,
            extraUserFollowing: Boolean): PendingIntent {
        // Setup delete intent
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.action = IntentConstants.BROADCAST_NOTIFICATION_DELETED
        val linkBuilder = Uri.Builder()
        linkBuilder.scheme(SCHEME_TWIDERE)
        linkBuilder.authority(AUTHORITY_INTERACTIONS)
        linkBuilder.appendPath(type)
        if (accountKey != null) {
            linkBuilder.appendQueryParameter(QUERY_PARAM_ACCOUNT_KEY, accountKey.toString())
        }
        linkBuilder.appendQueryParameter(QUERY_PARAM_READ_POSITION, position.toString())
        linkBuilder.appendQueryParameter(QUERY_PARAM_TIMESTAMP, System.currentTimeMillis().toString())
        linkBuilder.appendQueryParameter(QUERY_PARAM_NOTIFICATION_TYPE, type)

        UriExtraUtils.addExtra(linkBuilder, "item_id", extraId)
        UriExtraUtils.addExtra(linkBuilder, "item_user_id", extraUserId)
        UriExtraUtils.addExtra(linkBuilder, "item_user_following", extraUserFollowing)
        intent.data = linkBuilder.build()
        return PendingIntent.getBroadcast(context, 0, intent, 0)
    }
}