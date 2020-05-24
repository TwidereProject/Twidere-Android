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

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.mastodon.model.Account;
import org.mariotaku.microblog.library.mastodon.model.Card;
import org.mariotaku.microblog.library.mastodon.model.Context;
import org.mariotaku.microblog.library.mastodon.model.LinkHeaderList;
import org.mariotaku.microblog.library.mastodon.model.Status;
import org.mariotaku.microblog.library.mastodon.model.StatusUpdate;
import org.mariotaku.microblog.library.twitter.model.ResponseCode;
import org.mariotaku.restfu.annotation.method.DELETE;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Path;

/**
 * Created by mariotaku on 2017/4/17.
 */

public interface StatusesResources {
    @GET("/v1/statuses/{id}")
    Status fetchStatus(@Path("id") String id) throws MicroBlogException;

    @GET("/v1/statuses/{id}/context")
    Context getStatusContext(@Path("id") String id) throws MicroBlogException;

    @GET("/v1/statuses/{id}/card")
    Card getStatusCard(@Path("id") String id) throws MicroBlogException;

    @GET("/v1/statuses/{id}/reblogged_by")
    LinkHeaderList<Account> getStatusRebloggedBy(@Path("id") String id) throws MicroBlogException;

    @GET("/v1/statuses/{id}/favourited_by")
    LinkHeaderList<Account> getStatusFavouritedBy(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/statuses")
    Status postStatus(@Param StatusUpdate update) throws MicroBlogException;

    @DELETE("/v1/statuses/{id}")
    ResponseCode deleteStatus(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/statuses/{id}/reblog")
    Status reblogStatus(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/statuses/{id}/unreblog")
    Status unreblogStatus(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/statuses/{id}/favourite")
    Status favouriteStatus(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/statuses/{id}/unfavourite")
    Status unfavouriteStatus(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/statuses/{id}/pin")
    Status pinStatus(@Path("id") String id) throws MicroBlogException;

    @POST("/v1/statuses/{id}/unpin")
    Status unpinStatus(@Path("id") String id) throws MicroBlogException;
}
