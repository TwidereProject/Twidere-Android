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
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred

class DeferredTarget<R>(
        width: Int = Target.SIZE_ORIGINAL, height: Int = Target.SIZE_ORIGINAL,
        private val deferredInstance: Deferred<R, Exception> = deferred()
) : SimpleTarget<R>(width, height) {

    val promise: Promise<R, Exception> get() = deferredInstance.promise

    override fun onLoadFailed(e: Exception, errorDrawable: Drawable?) {
        if (deferredInstance.promise.isDone()) return
        deferredInstance.reject(e)
    }

    override fun onResourceReady(resource: R, glideAnimation: GlideAnimation<in R>) {
        if (deferredInstance.promise.isDone()) return
        deferredInstance.resolve(resource)
    }

}
