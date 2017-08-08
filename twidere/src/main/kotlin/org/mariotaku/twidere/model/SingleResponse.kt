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

package org.mariotaku.twidere.model

import android.os.Bundle

data class SingleResponse<Data>(
        override val data: Data? = null,
        override val exception: Exception? = null,
        override val extras: Bundle = Bundle()
) : Response<Data> {

    constructor(data: Data) : this(data, null)

    constructor(exception: Exception) : this(null, exception)

    override fun hasData(): Boolean {
        return data != null
    }

    override fun hasException(): Boolean {
        return exception != null
    }

    companion object {

        fun <T> getInstance(): SingleResponse<T> {
            return SingleResponse(null, null)
        }

        fun <T> getInstance(exception: Exception): SingleResponse<T> {
            return SingleResponse(null, exception)
        }

        fun <T> getInstance(data: T): SingleResponse<T> {
            return SingleResponse(data, null)
        }

        fun <T> getInstance(data: T, exception: Exception): SingleResponse<T> {
            return SingleResponse(data, exception)
        }
    }
}
