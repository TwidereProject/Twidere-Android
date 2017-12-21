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
import org.mariotaku.microblog.library.model.microblog.PageableResponseList;
import org.mariotaku.microblog.library.model.microblog.Paging;
import org.mariotaku.microblog.library.model.microblog.ResponseList;
import org.mariotaku.microblog.library.model.microblog.Status;
import org.mariotaku.microblog.library.model.microblog.StatusActivitySummary;
import org.mariotaku.microblog.library.model.microblog.TranslationResult;
import org.mariotaku.microblog.library.model.microblog.User;
import org.mariotaku.microblog.library.twitter.template.StatusAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.restfu.annotation.param.Query;

@SuppressWarnings("RedundantThrows")
@Params(template = StatusAnnotationTemplate.class)
public interface PrivateTweetResources extends PrivateResources {

    @GET("/statuses/{id}/activity/summary.json")
    StatusActivitySummary getStatusActivitySummary(@Path("id") String statusId) throws MicroBlogException;

    @GET("/statuses/favorited_by.json")
    PageableResponseList<User> getFavoritedBy(@Query("id") String statusId, @Query Paging paging) throws MicroBlogException;

    @GET("/statuses/retweeted_by.json")
    PageableResponseList<User> getRetweetedBy(@Query("id") String statusId, @Query Paging paging) throws MicroBlogException;

    @GET("/conversation/show.json")
    ResponseList<Status> showConversation(@Query("id") String statusId, @Query Paging paging) throws MicroBlogException;

    @GET("/translations/show.json")
    TranslationResult showTranslation(@Query("id") String statusId, @Query("dest") String dest) throws MicroBlogException;
}
