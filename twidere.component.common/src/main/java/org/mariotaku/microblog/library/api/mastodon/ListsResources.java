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

package org.mariotaku.microblog.library.api.mastodon;

import android.support.annotation.NonNull;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.model.mastodon.LinkHeaderList;
import org.mariotaku.microblog.library.model.mastodon.List;
import org.mariotaku.microblog.library.model.Paging;
import org.mariotaku.restfu.annotation.method.DELETE;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.method.PUT;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Path;

public interface ListsResources {
    @GET("/v1/lists")
    LinkHeaderList<List> getLists(@Param Paging paging) throws MicroBlogException;

    @GET("/v1/accounts/{id}/lists")
    LinkHeaderList<List> getListsMembership(@Path("id") @NonNull String id) throws MicroBlogException;

    @GET("/v1/lists/{id}/accounts")
    LinkHeaderList<List> getListAccounts(@Path("id") @NonNull String id) throws MicroBlogException;

    @GET("/v1/lists/{id}")
    List getList(@Path("id") @NonNull String id) throws MicroBlogException;

    @POST("/v1/lists")
    List createList(@Param("title") @NonNull String title) throws MicroBlogException;

    @PUT("/v1/lists/{id}")
    List updateList(@Path("id") @NonNull String id, @Param("title") @NonNull String title) throws MicroBlogException;

    @DELETE("/v1/lists/{id}")
    List deleteList(@Path("id") @NonNull String id) throws MicroBlogException;

    @POST("/v1/lists/{id}/accounts")
    List addListAccounts(@Path("id") @NonNull String id, @Param("account_ids") @NonNull String[] ids) throws MicroBlogException;

    @DELETE("/v1/lists/{id}/accounts")
    List deleteListAccounts(@Path("id") @NonNull String id, @Param("account_ids") @NonNull String[] ids) throws MicroBlogException;
}
