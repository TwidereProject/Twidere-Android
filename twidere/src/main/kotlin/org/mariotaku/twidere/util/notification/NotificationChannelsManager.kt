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
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import org.mariotaku.kpreferences.get
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.model.notificationChannelGroupId
import org.mariotaku.twidere.extension.model.notificationChannelId
import org.mariotaku.twidere.model.notification.NotificationChannelSpec
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.dagger.DependencyHolder

/**
 * Created by mariotaku on 2017/8/25.
 */
object NotificationChannelsManager {

    fun initialize(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        NotificationChannelManagerImpl.initialize(context)
    }

    fun updateAccountChannelsAndGroups(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        NotificationChannelManagerImpl.updateAccountChannelsAndGroups(context)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private object NotificationChannelManagerImpl {

        fun initialize(context: Context) {
            val nm = context.getSystemService(NotificationManager::class.java)

            val addedChannels = mutableListOf<String>()

            for (spec in NotificationChannelSpec.values()) {
                if (spec.grouped) continue
                val channel = NotificationChannel(spec.id, context.getString(spec.nameRes), spec.importance)

                if (spec.descriptionRes != 0) {
                    channel.description = context.getString(spec.descriptionRes)
                }
                channel.setShowBadge(spec.showBadge)
                nm.createNotificationChannel(channel)
                addedChannels.add(channel.id)
            }

            nm.notificationChannels.forEach {
                if (it.id !in addedChannels && it.group == null) {
                    nm.deleteNotificationChannel(it.id)
                }
            }
        }

        fun updateAccountChannelsAndGroups(context: Context) {
            val holder = DependencyHolder.get(context)

            val am = AccountManager.get(context)
            val nm = context.getSystemService(NotificationManager::class.java)
            val pref = holder.preferences
            val ucnm = holder.userColorNameManager

            val accounts = AccountUtils.getAllAccountDetails(am, false)
            val specs = NotificationChannelSpec.values()

            val addedChannels = mutableListOf<String>()
            val addedGroups = mutableListOf<String>()

            accounts.forEach { account ->
                val group = NotificationChannelGroup(account.key.notificationChannelGroupId(),
                        ucnm.getDisplayName(account.user, pref[nameFirstKey]))

                addedGroups.add(group.id)
                nm.createNotificationChannelGroup(group)

                for (spec in specs) {
                    if (!spec.grouped) continue
                    val channel = NotificationChannel(account.key.notificationChannelId(spec.id),
                            context.getString(spec.nameRes), spec.importance)

                    if (spec.descriptionRes != 0) {
                        channel.description = context.getString(spec.descriptionRes)
                    }
                    channel.group = group.id
                    channel.setShowBadge(spec.showBadge)
                    nm.createNotificationChannel(channel)
                    addedChannels.add(channel.id)
                }

            }

            // Delete all channels and groups of non-existing accounts
            nm.notificationChannels.forEach {
                if (it.id !in addedChannels && it.group != null) {
                    nm.deleteNotificationChannel(it.id)
                }
            }
            nm.notificationChannelGroups.forEach {
                if (it.id !in addedGroups) {
                    nm.deleteNotificationChannelGroup(it.id)
                }
            }
        }
    }
}