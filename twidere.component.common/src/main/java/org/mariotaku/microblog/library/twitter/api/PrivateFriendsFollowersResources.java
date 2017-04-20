/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
