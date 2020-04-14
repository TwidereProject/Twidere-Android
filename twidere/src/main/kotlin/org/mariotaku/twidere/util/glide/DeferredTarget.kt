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

package org.mariotaku.twidere.util.glide

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred

/**
 * Created by mariotaku on 2017/3/21.
 */

class DeferredTarget<R>(private val deferredInstance: Deferred<R, Exception> = deferred()) : CustomTarget<R>() {

    val promise: Promise<R, Exception> get() = deferredInstance.promise

    override fun onLoadFailed(errorDrawable: Drawable?) {
        if (deferredInstance.promise.isDone()) return
        deferredInstance.reject(Exception())
    }

    override fun onResourceReady(resource: R, transition: Transition<in R>?) {
        if (deferredInstance.promise.isDone()) return
        deferredInstance.resolve(resource)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        if (deferredInstance.promise.isDone()) return
        deferredInstance.reject(Exception())
    }

}
