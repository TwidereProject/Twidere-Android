/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.loader

import android.content.Context
import android.support.v4.content.AsyncTaskLoader
import android.text.TextUtils
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.loader.iface.IExtendedLoader
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.util.collection.NoDuplicatesArrayList
import java.util.*

abstract class ParcelableUsersLoader(
        context: Context,
        data: List<ParcelableUser>?,
        override var fromUser: Boolean
) : AsyncTaskLoader<List<ParcelableUser>>(context), IExtendedLoader, Constants {

    protected val data: MutableList<ParcelableUser> = Collections.synchronizedList(NoDuplicatesArrayList<ParcelableUser>())

    init {
        if (data != null) {
            this.data.addAll(data)
        }
    }

    public override fun onStartLoading() {
        forceLoad()
    }

    protected fun hasId(id: String): Boolean {
        for (i in this.data.indices) {
            if (TextUtils.equals(this.data[i].key.id, id)) return true
        }
        return false
    }

}
