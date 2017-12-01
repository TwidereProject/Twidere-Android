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
import org.mariotaku.restfu.RestFuUtils
import org.mariotaku.restfu.RestRequest
import org.mariotaku.restfu.http.Authorization
import org.mariotaku.restfu.http.Endpoint
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.restfu.http.MultiValueMap
import org.mariotaku.twidere.util.MicroBlogAPIFactory

/**
 * Created by mariotaku on 2017/4/11.
 */
class TwidereHttpRequestFactory(
        private val extraHeaders: MicroBlogAPIFactory.ExtraHeaders?
) : HttpRequest.Factory<MicroBlogException> {

    override fun create(endpoint: Endpoint, info: RestRequest, authorization: Authorization?,
            converterFactory: RestConverter.Factory<MicroBlogException>): HttpRequest {
        val restMethod = info.method
        val url = Endpoint.constructUrl(endpoint.url, info)
        val headers = info.headers ?: MultiValueMap()

        if (authorization != null && authorization.hasAuthorization()) {
            headers.add("Authorization", RestFuUtils.sanitizeHeader(authorization.getHeader(endpoint, info)))
        }
        if (extraHeaders != null) {
            for ((first, second) in extraHeaders[headers]) {
                headers.add(first, RestFuUtils.sanitizeHeader(second))
            }
        }
        return HttpRequest(restMethod, url, headers, info.getBody(converterFactory), null)
    }
}
