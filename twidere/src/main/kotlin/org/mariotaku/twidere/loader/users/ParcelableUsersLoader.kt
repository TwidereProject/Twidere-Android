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

package org.mariotaku.twidere.loader.users

import android.content.Context
import androidx.loader.content.FixedAsyncTaskLoader
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList
import java.util.*

abstract class ParcelableUsersLoader(
        context: Context,
        data: List<ParcelableUser>?,
        override var fromUser: Boolean
) : FixedAsyncTaskLoader<List<ParcelableUser>>(context), IExtendedLoader {

    protected val data: MutableList<ParcelableUser> = Collections.synchronizedList(NoDuplicatesArrayList<ParcelableUser>())

    init {
        if (data != null) {
            this.data.addAll(data)
        }
    }

    override fun onStartLoading() {
        forceLoad()
    }

    protected fun hasId(key: UserKey): Boolean {
        return data.indices.any { data[it].key == key }
    }

}
