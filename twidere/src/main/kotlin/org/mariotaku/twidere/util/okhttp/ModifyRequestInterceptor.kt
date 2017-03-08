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

package org.mariotaku.twidere.util.okhttp

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class ModifyRequestInterceptor(vararg val modifiers: RequestModifier) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val rb = request.newBuilder()
        modifiers.forEach { it.modify(request, rb) }
        return chain.proceed(rb.build())
    }

    interface RequestModifier {
        fun modify(original: Request, builder: Request.Builder): Boolean
    }
}