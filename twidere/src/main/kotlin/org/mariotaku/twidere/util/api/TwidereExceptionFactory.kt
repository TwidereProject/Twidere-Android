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

import com.fasterxml.jackson.core.JsonParseException
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.restfu.ExceptionFactory
import org.mariotaku.restfu.RestConverter
import org.mariotaku.restfu.http.HttpRequest
import org.mariotaku.restfu.http.HttpResponse
import java.io.IOException

/**
 * Create `MicroBlogException`
 *
 * Created by mariotaku on 2017/4/11.
 */
object TwidereExceptionFactory : ExceptionFactory<MicroBlogException> {

    override fun newException(cause: Throwable?, request: HttpRequest?, response: HttpResponse?): MicroBlogException {
        val te = when {
            cause != null -> {
                MicroBlogException(cause)
            }
            response != null -> {
                parseTwitterException(response)
            }
            else -> {
                MicroBlogException()
            }
        }
        te.httpRequest = request
        te.httpResponse = response
        return te
    }


    fun parseTwitterException(resp: HttpResponse): MicroBlogException {
        return try {
            val converter = TwitterConverterFactory.forResponse(MicroBlogException::class.java)
            converter.convert(resp) as MicroBlogException
        } catch (e: JsonParseException) {
            MicroBlogException("Malformed JSON Data", e)
        } catch (e: IOException) {
            MicroBlogException("IOException while throwing exception", e)
        } catch (e: RestConverter.ConvertException) {
            MicroBlogException(e)
        } catch (e: MicroBlogException) {
            e
        }

    }
}
