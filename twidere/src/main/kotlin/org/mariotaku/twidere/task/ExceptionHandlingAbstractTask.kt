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

package org.mariotaku.twidere.task

import android.content.Context
import org.mariotaku.twidere.model.SingleResponse

/**
 * Created by mariotaku on 2017/2/10.
 */

abstract class ExceptionHandlingAbstractTask<Params, Result, TaskException : Exception, Callback>(
        context: Context
) : BaseAbstractTask<Params, SingleResponse<Result>, Callback>(context) {

    protected abstract val exceptionClass: Class<TaskException>

    final override fun afterExecute(callback: Callback?, results: SingleResponse<Result>) {
        @Suppress("UNCHECKED_CAST")
        afterExecute(callback, results.data, results.exception as? TaskException)
        if (results.data != null) {
            onSucceed(callback, results.data)
        } else if (results.exception != null) {
            if (exceptionClass.isInstance(results.exception)) {
                @Suppress("UNCHECKED_CAST")
                onException(callback, results.exception as TaskException)
            } else {
                throw results.exception
            }
        }
    }

    final override fun doLongOperation(params: Params): SingleResponse<Result> {
        try {
            return SingleResponse(onExecute(params))
        } catch (tr: Exception) {
            if (exceptionClass.isInstance(tr)) return SingleResponse(tr)
            throw tr
        }
    }

    open fun afterExecute(callback: Callback?, result: Result?, exception: TaskException?) {
    }

    open fun onSucceed(callback: Callback?, result: Result) {
    }

    open fun onException(callback: Callback?, exception: TaskException) {
    }

    abstract fun onExecute(params: Params): Result
}
