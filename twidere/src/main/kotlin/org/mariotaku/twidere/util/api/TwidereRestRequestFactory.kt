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

package org.mariotaku.twidere.util.api

import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.restfu.RestConverter
import org.mariotaku.restfu.RestMethod
import org.mariotaku.restfu.RestRequest
import org.mariotaku.restfu.http.ValueMap

/**
 * Convert rest method to rest request
 *
 * Created by mariotaku on 2017/4/11.
 */
class TwidereRestRequestFactory(
        private val extraRequestParams: Map<String, String>?
) : RestRequest.Factory<MicroBlogException> {

    override fun create(restMethod: RestMethod<MicroBlogException>,
            factory: RestConverter.Factory<MicroBlogException>, valuePool: ValueMap?): RestRequest {
        val method = restMethod.method
        val path = restMethod.path
        val headers = restMethod.getHeaders(valuePool)
        val queries = restMethod.getQueries(valuePool)
        val params = restMethod.getParams(factory, valuePool)
        val rawValue = restMethod.rawValue
        val bodyType = restMethod.bodyType
        val extras = restMethod.extras

        if (extraRequestParams != null) {
            for ((key, value) in extraRequestParams) {
                queries.add(key, value)
            }
        }

        return RestRequest(method.value, method.allowBody, path, headers, queries,
                params, rawValue, bodyType, extras)
    }
}
