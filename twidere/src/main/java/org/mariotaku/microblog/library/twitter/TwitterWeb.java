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

package org.mariotaku.microblog.library.twitter;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.FavoritedPopup;
import org.mariotaku.microblog.library.twitter.model.StatusPage;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Headers;
import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.restfu.annotation.param.Query;

/**
 * Created by mariotaku on 2017/4/1.
 */

public interface TwitterWeb {
    @GET("/i/activity/favorited_popup")
    @Headers({@KeyValue(key = "Accept", value = "application/json"),
            @KeyValue(key = "X-Requested-With", value = "XMLHttpRequest"),
            @KeyValue(key = "User-Agent", value = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36")})
    FavoritedPopup getFavoritedPopup(@Query("id") String statusId) throws MicroBlogException;

    @GET("/{screen_name}/status/{id}")
    @Headers({@KeyValue(key = "Accept", value = "application/json, text/javascript, */*; q=0.01"),
            @KeyValue(key = "X-Overlay-Request", value = "true"),
            @KeyValue(key = "X-Requested-With", value = "XMLHttpRequest"),
            @KeyValue(key = "User-Agent", value = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36")})
    StatusPage getStatusPage(@Path("screen_name") String screenName, @Path("id") String statusId)
            throws MicroBlogException;
}
