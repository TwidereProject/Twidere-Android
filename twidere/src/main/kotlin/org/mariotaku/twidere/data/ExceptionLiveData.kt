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

package org.mariotaku.twidere.data

import android.arch.lifecycle.LiveData
import org.mariotaku.twidere.model.SingleResponse

abstract class ExceptionLiveData<T> : LiveData<SingleResponse<T>>() {

    protected fun setData(data: T) {
        value = SingleResponse(data)
    }

    protected fun setException(exception: Exception) {
        value = SingleResponse(exception)
    }

    protected fun postData(data: T) {
        postValue(SingleResponse(data))
    }

    protected fun postException(exception: Exception) {
        postValue(SingleResponse(exception))
    }

    companion object {
        fun <T> wrap(liveData: LiveData<T>): ExceptionLiveData<T> {
            val wrapped = object : ExceptionLiveData<T>() {}
            liveData.observeForever {
                if (it != null) {
                    wrapped.postData(it)
                } else {
                    wrapped.postException(NullPointerException())
                }
            }
            return wrapped
        }
    }

}
