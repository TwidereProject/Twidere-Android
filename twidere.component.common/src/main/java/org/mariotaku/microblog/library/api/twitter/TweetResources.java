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

package org.mariotaku.microblog.library.api.twitter;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.model.microblog.IDs;
import org.mariotaku.microblog.library.model.microblog.Paging;
import org.mariotaku.microblog.library.model.microblog.ResponseList;
import org.mariotaku.microblog.library.model.microblog.Status;
import org.mariotaku.microblog.library.model.microblog.StatusUpdate;
import org.mariotaku.microblog.library.twitter.template.StatusAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.restfu.annotation.param.Query;

@SuppressWarnings("RedundantThrows")
@Params(template = StatusAnnotationTemplate.class)
public interface TweetResources {
    @POST("/statuses/destroy/{id}.json")
    Status destroyStatus(@Path("id") String statusId) throws MicroBlogException;

    @GET("/statuses/retweeters/ids.json")
    IDs getRetweetersIDs(@Query("id") String statusId, @Query Paging paging) throws MicroBlogException;

    @GET("/statuses/retweets/{id}.json")
    ResponseList<Status> getRetweets(@Path("id") String statusId, @Query Paging paging) throws MicroBlogException;

    @POST("/statuses/retweet/{id}.json")
    Status retweetStatus(@Path("id") String statusId) throws MicroBlogException;

    @POST("/statuses/unretweet/{id}.json")
    Status unretweetStatus(@Path("id") String statusId) throws MicroBlogException;

    @GET("/statuses/show.json")
    Status showStatus(@Query("id") String id) throws MicroBlogException;

    @POST("/statuses/update.json")
    Status updateStatus(@Param StatusUpdate latestStatus) throws MicroBlogException;

    @POST("/statuses/lookup.json")
    ResponseList<Status> lookupStatuses(@Param(value = "id", arrayDelimiter = ',') String[] ids) throws MicroBlogException;

}
