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

package org.mariotaku.twidere.api.twitter.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Body;
import org.mariotaku.restfu.annotation.param.Form;
import org.mariotaku.restfu.annotation.param.MethodExtra;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
@SuppressWarnings("RedundantThrows")
@MethodExtra(name = "extra_params", values = {"include_my_retweet", "include_rts", "include_entities",
        "include_cards", "cards_platform", "include_reply_count", "include_descendent_reply_count"})
public interface FavoritesResources {

    @POST("/favorites/create.json")
    @Body(BodyType.FORM)
    Status createFavorite(@Form("id") long id) throws TwitterException;

    @POST("/favorites/destroy.json")
    @Body(BodyType.FORM)
    Status destroyFavorite(@Form("id") long id) throws TwitterException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites() throws TwitterException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites(@Query("user_id") long userId) throws TwitterException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites(@Query("user_id") long userId, @Query({"since_id", "max_id", "count"}) Paging paging) throws TwitterException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites(@Query({"since_id", "max_id", "count"}) Paging paging) throws TwitterException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites(@Query("screen_name") String screenName) throws TwitterException;

    @GET("/favorites/list.json")
    ResponseList<Status> getFavorites(@Query("screen_name") String screenName, @Query({"since_id", "max_id", "count"}) Paging paging) throws TwitterException;
}
