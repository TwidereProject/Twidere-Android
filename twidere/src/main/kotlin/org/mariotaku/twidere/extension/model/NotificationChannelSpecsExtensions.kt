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

package org.mariotaku.twidere.extension.model

import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationCompat
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.notification.NotificationChannelSpec

fun NotificationChannelSpec.notificationBuilder(context: Context): NotificationCompat.Builder {
    return NotificationCompat.Builder(context, id)
}

fun NotificationChannelSpec.accountNotificationBuilder(context: Context, accountKey: UserKey): NotificationCompat.Builder {
    if (!grouped) throw IllegalArgumentException("Requires grouped channel")
    return NotificationCompat.Builder(context, accountKey.notificationChannelId(id)).setGroup(accountKey.notificationChannelGroupId())
}

fun UserKey.notificationChannelId(id: String): String {
    return "${id}_${Uri.encode(toString())}"
}

fun UserKey.notificationChannelGroupId(): String {
    return Uri.encode(toString())
}

fun NotificationChannelSpec.getName(context: Context): String {
    return context.getString(nameRes)
}

fun NotificationChannelSpec.getDescription(context: Context): String? {
    if (descriptionRes == 0) return null
    return context.getString(descriptionRes)
}