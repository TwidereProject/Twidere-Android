/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.microblog.library.twitter.model;

import org.mariotaku.restfu.RestConverter;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.microblog.library.MicroBlogException;

/**
 * Created by mariotaku on 15/6/15.
 */
public class ResponseCode {

    private final int responseCode;

    public ResponseCode(HttpResponse response) {
        responseCode = response.getStatus();
    }

    public int getResponseCode() {
        return responseCode;
    }

    public boolean isSuccessful() {
        return responseCode >= 200 && responseCode < 300;
    }

    public static class ResponseConverter implements RestConverter<HttpResponse, ResponseCode, MicroBlogException> {

        @Override
        public ResponseCode convert(HttpResponse response) {
            return new ResponseCode(response);
        }
    }
}
