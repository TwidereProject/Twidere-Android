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

package org.mariotaku.twidere.util.paging

import android.support.v7.recyclerview.extensions.DiffCallback
import org.mariotaku.twidere.model.ParcelableActivity
import org.mariotaku.twidere.model.ParcelableStatus

object DiffCallbacks {
    val status: DiffCallback<ParcelableStatus> = object : DiffCallback<ParcelableStatus>() {
        override fun areContentsTheSame(oldItem: ParcelableStatus, newItem: ParcelableStatus): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: ParcelableStatus, newItem: ParcelableStatus): Boolean {
            if (oldItem._id > 0 && newItem._id > 0) return oldItem._id == newItem._id
            return oldItem == newItem
        }

    }
    val activity: DiffCallback<ParcelableActivity> = object : DiffCallback<ParcelableActivity>() {
        override fun areContentsTheSame(oldItem: ParcelableActivity, newItem: ParcelableActivity): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: ParcelableActivity, newItem: ParcelableActivity): Boolean {
            if (oldItem._id > 0 && newItem._id > 0) return oldItem._id == newItem._id
            return oldItem == newItem
        }

    }
}