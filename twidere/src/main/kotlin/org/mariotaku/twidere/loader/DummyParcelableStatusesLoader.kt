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

import org.mariotaku.twidere.model.ListResponse
import org.mariotaku.twidere.model.ParcelableStatus

class DummyParcelableStatusesLoader @JvmOverloads constructor(context: Context, data: List<ParcelableStatus>? = null, fromUser: Boolean = false) : ParcelableStatusesLoader(context, data, -1, fromUser) {

    override fun loadInBackground(): ListResponse<ParcelableStatus> {
        return ListResponse.getListInstance(data)
    }

}
