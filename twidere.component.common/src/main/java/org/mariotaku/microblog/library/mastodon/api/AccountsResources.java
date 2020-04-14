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

package org.mariotaku.microblog.library.mastodon.api;

import androidx.annotation.Nullable;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.mastodon.model.Account;
import org.mariotaku.microblog.library.mastodon.model.AccountUpdate;
import org.mariotaku.microblog.library.mastodon.model.LinkHeaderList;
import org.mariotaku.microblog.library.mastodon.model.Relationship;
import org.mariotaku.microblog.library.mastodon.model.Status;
import org.mariotaku.microblog.library.mastodon.model.TimelineOption;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.PATCH;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.restfu.annotation.param.Query;

/**
 * Created by mariotaku on 2017/4/17.
 */

public interface AccountsResources {

    @GET("/v1/accounts/{id}")
    Account getAccount(@Path("id") String id) throws MicroBlogException;

    @GET("/v1/accounts/verify_credentials")
    Account verifyCredentials() throws MicroBlogException;

    @PATCH("/v1/accounts/update_credentials")
    Account updateCredentials(@Param AccountUpdate update) throws MicroBlogException;

    @GET("/v1/accounts/{id}/followers")
    LinkHeaderList<Account> getFollowers(@Path("id") String id, @Query Paging paging)
            throws MicroBlogException;

    @GET("/v1/accounts/{id}/following")
    LinkHeaderList<Account> getFollowing(@Path("id") String id, @Query Paging paging)
            throws MicroBlogException;

    @GET("/v1/accounts/{id}/statuses")
    LinkHeaderList<Status> getStatuses(@Path("id") String id, @Query Paging paging,
            @Query TimelineOption option) throws MicroBlogException;

    @POST("/v1/accounts/{id}/follow")
    Relationship followUser(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/accounts/{id}/unfollow")
    Relationship unfollowUser(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/accounts/{id}/block")
    Relationship blockUser(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/accounts/{id}/unblock")
    Relationship unblockUser(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/accounts/{id}/mute")
    Relationship muteUser(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/accounts/{id}/unmute")
    Relationship unmuteUser(@Path("id") String id) throws MicroBlogException;

    @GET("/v1/accounts/relationships")
    LinkHeaderList<Relationship> getRelationships(@Query(value = "id[]")
            String[] id) throws MicroBlogException;

    @GET("/v1/accounts/search")
    LinkHeaderList<Account> searchAccounts(@Query("q") String query, @Nullable @Query Paging paging) throws MicroBlogException;
}
