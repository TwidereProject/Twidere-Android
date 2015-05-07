/*
 * Twidere - Twitter client for Android
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

package twitter4j.api;

import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.param.Query;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface FavoritesResources {
    /**
     * Favorites the status specified in the ID parameter as the authenticating
     * user. Returns the favorite status when successful. <br>
     * This method calls http://api.twitter.com/1.1/favorites/create/[id].json
     *
     * @param id the ID of the status to favorite
     * @return Status
     * @throws twitter4j.TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/favorites/create/:id">POST
     * favorites/create/:id | Twitter Developers</a>
     */
    Status createFavorite(long id) throws TwitterException;

    /**
     * Un-favorites the status specified in the ID parameter as the
     * authenticating user. Returns the un-favorited status in the requested
     * format when successful. <br>
     * This method calls http://api.twitter.com/1.1/favorites/destroy/[id].json
     *
     * @param id the ID of the status to un-favorite
     * @return Status
     * @throws twitter4j.TwitterException when Twitter service or network is unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/post/favorites/destroy/:id">POST
     * favorites/destroy/:id | Twitter Developers</a>
     */
    Status destroyFavorite(long id) throws TwitterException;

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
