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

package org.mariotaku.twidere.model.notification

import android.app.NotificationManager
import androidx.annotation.StringRes
import org.mariotaku.twidere.R

/**
 * Created by mariotaku on 2017/8/25.
 */
enum class NotificationChannelSpec(
        val id: String,
        @StringRes val nameRes: Int,
        @StringRes val descriptionRes: Int = 0,
        val importance: Int,
        val showBadge: Boolean = false,
        val grouped: Boolean = false) {
    /**
     * For notifications send by app itself.
     * Such as "what's new"
     */
    appNotices("app_notices", R.string.notification_channel_name_app_notices,
            importance = NotificationManager.IMPORTANCE_LOW, showBadge = true),

    /**
     * For notifications indicate that some lengthy operations are performing in the background.
     * Such as sending attachment process.
     */
    backgroundProgresses("background_progresses", R.string.notification_channel_name_background_progresses,
            importance = NotificationManager.IMPORTANCE_MIN),

    /**
     * For updates related to micro-blogging features.
     * Such as new statuses posted by friends.
     */
    contentUpdates("content_updates", R.string.notification_channel_name_content_updates,
            descriptionRes = R.string.notification_channel_descriptions_content_updates,
            importance = NotificationManager.IMPORTANCE_DEFAULT, showBadge = true, grouped = true),
    /**
     * For updates related to micro-blogging features.
     * Such as new statuses posted by friends user subscribed to.
     */
    contentSubscriptions("content_subscriptions", R.string.notification_channel_name_content_subscriptions,
            descriptionRes = R.string.notification_channel_descriptions_content_subscriptions,
            importance = NotificationManager.IMPORTANCE_HIGH, showBadge = true, grouped = true),
    /**
     * For interactions related to micro-blogging features.
     * Such as replies and likes.
     */
    contentInteractions("content_interactions", R.string.notification_channel_name_content_interactions,
            descriptionRes = R.string.notification_channel_description_content_interactions,
            importance = NotificationManager.IMPORTANCE_HIGH, showBadge = true, grouped = true),
    /**
     * For messages related to micro-blogging features.
     * Such as direct messages.
     */
    contentMessages("content_messages", R.string.notification_channel_name_content_messages,
            descriptionRes = R.string.notification_channel_description_content_messages,
            importance = NotificationManager.IMPORTANCE_HIGH, showBadge = true, grouped = true)

}