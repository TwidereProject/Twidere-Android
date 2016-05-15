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

package org.mariotaku.microblog.library.twitter.api;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Queries;

@Queries({@KeyValue(key = "include_entities", valueKey = "include_entities")})
public interface PrivateFriendsFollowersResources extends PrivateResources {

    @POST("/friendships/accept.json")
    User acceptFriendship(@Param("user_id") String userId) throws MicroBlogException;

    @POST("/friendships/accept.json")
    User acceptFriendshipByScreenName(@Param("screen_name") String screenName) throws MicroBlogException;

    @POST("/friendships/deny.json")
    User denyFriendship(@Param("user_id") String userId) throws MicroBlogException;

    @POST("/friendships/deny.json")
    User denyFriendshipByScreenName(@Param("screen_name") String screenName) throws MicroBlogException;

}
