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
import org.mariotaku.microblog.library.twitter.model.DirectMessageEventObject;
import org.mariotaku.microblog.library.twitter.model.PageableResponseList;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseCode;
import org.mariotaku.microblog.library.twitter.template.DirectMessageAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.DELETE;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.annotation.param.Raw;
import org.mariotaku.restfu.http.BodyType;

/**
 * Created by mariotaku on 2017/5/11.
 */
@Params(template = DirectMessageAnnotationTemplate.class)
public interface DirectMessagesEventResources {

    @POST("/direct_messages/events/new.json")
    @BodyType(BodyType.RAW)
    DirectMessageEventObject newDirectMessageEvent(@Raw(contentType = "application/json", encoding = "UTF-8")
            DirectMessageEventObject event) throws MicroBlogException;

    @GET("/direct_messages/events/list.json")
    PageableResponseList<DirectMessageEventObject.Event> getDirectMessageList(@Query int count, @Query Paging paging) throws MicroBlogException;

    @GET("/direct_messages/events/show.json")
    DirectMessageEventObject showDirectMessageEvent(@Query("id") String id) throws MicroBlogException;

    @DELETE("/direct_messages/events/destroy.json")
    ResponseCode destroyDirectMessage(@Param("id") String id) throws MicroBlogException;
}
