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

package org.mariotaku.twidere.loader.statuses

import android.content.Context
import androidx.loader.content.FixedAsyncTaskLoader
import android.text.TextUtils
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList

abstract class ParcelableStatusesLoader(
        context: Context,
        adapterData: List<ParcelableStatus>?,
        protected val tabPosition: Int,
        override var fromUser: Boolean
) : FixedAsyncTaskLoader<List<ParcelableStatus>>(context), IExtendedLoader {

    protected val data = NoDuplicatesArrayList<ParcelableStatus>()
    protected val isFirstLoad: Boolean = adapterData == null

    init {
        if (adapterData != null) {
            data.addAll(adapterData)
        }
    }

    protected fun containsStatus(statusId: String): Boolean {
        return data.any { TextUtils.equals(it.id, statusId) }
    }

    protected fun deleteStatus(statuses: MutableList<ParcelableStatus>?, statusId: String): Boolean {
        if (statuses == null || statuses.isEmpty()) return false
        var result = false
        for (i in statuses.indices.reversed()) {
            if (TextUtils.equals(statuses[i].id, statusId)) {
                statuses.removeAt(i)
                result = true
            }
        }
        return result
    }

    override fun onStartLoading() {
        forceLoad()
    }


}
