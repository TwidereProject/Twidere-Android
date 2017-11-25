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

package org.mariotaku.twidere.extension.promise

import com.squareup.otto.Bus
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.ui.alwaysUi
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.promiseOnUi
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.promise.FriendshipPromises

fun notifyCreatePromise(bus: Bus, @FriendshipTaskEvent.Action action: Int, accountKey: UserKey, userKey: UserKey) = promiseOnUi {
    FriendshipPromises.addTask(accountKey, userKey)
    val event = FriendshipTaskEvent(action, accountKey, userKey)
    event.isFinished = false
    bus.post(event)
}

fun Promise<ParcelableUser, Exception>.notifyOnResult(bus: Bus, @FriendshipTaskEvent.Action action: Int, accountKey: UserKey, userKey: UserKey) = successUi { user ->
    val event = FriendshipTaskEvent(action, accountKey, userKey)
    event.isFinished = true
    event.isSucceeded = true
    event.user = user
    bus.post(event)
}.failUi { ex ->
    val event = FriendshipTaskEvent(action, accountKey, userKey)
    event.isFinished = true
    event.isSucceeded = false
    bus.post(event)
}.alwaysUi {
    FriendshipPromises.removeTask(accountKey, userKey)
}