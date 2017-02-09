/*
 *         Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mariotaku.microblog.library.twitter.api;

import org.mariotaku.microblog.library.twitter.template.StatusAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Queries;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Activity;
import org.mariotaku.microblog.library.twitter.model.CursorTimestampResponse;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;

@SuppressWarnings("RedundantThrows")
@Queries(template = StatusAnnotationTemplate.class)
public interface PrivateActivityResources extends PrivateResources {

    @GET("/activity/about_me.json")
    ResponseList<Activity> getActivitiesAboutMe(@Query Paging paging) throws MicroBlogException;

    @Queries({})
    @GET("/activity/about_me/unread.json")
    CursorTimestampResponse getActivitiesAboutMeUnread(@Query("cursor") boolean cursor) throws MicroBlogException;

    @Queries({})
    @POST("/activity/about_me/unread.json")
    @BodyType(BodyType.FORM)
    CursorTimestampResponse setActivitiesAboutMeUnread(@Param("cursor") long cursor) throws MicroBlogException;


}
