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
import org.mariotaku.microblog.library.model.microblog.DirectMessageEventObject;
import org.mariotaku.microblog.library.model.microblog.PageableResponseList;
import org.mariotaku.microblog.library.model.microblog.Paging;
import org.mariotaku.microblog.library.model.microblog.ResponseCode;
import org.mariotaku.microblog.library.twitter.template.DirectMessageAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.DELETE;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.annotation.param.Raw;
import org.mariotaku.restfu.http.BodyType;

@Params(template = DirectMessageAnnotationTemplate.class)
public interface DirectMessagesEventResources {

    @GET("/direct_messages/events/list.json")
    @BodyType(BodyType.FORM)
    PageableResponseList<DirectMessageEventObject> getDirectMessageEvents(@Query Paging paging)
            throws MicroBlogException;

    @POST("/direct_messages/events/new.json")
    @BodyType(BodyType.RAW)
    DirectMessageEventObject newDirectMessageEvent(@Raw(contentType = "application/json", encoding = "UTF-8")
            DirectMessageEventObject event) throws MicroBlogException;

    @GET("/direct_messages/events/show.json")
    DirectMessageEventObject showDirectMessageEvent(@Query("id") String id)
            throws MicroBlogException;

    @DELETE("/direct_messages/events/destroy.json")
    @BodyType(BodyType.FORM)
    ResponseCode destroyDirectMessageEvent(@Param("id") String id) throws MicroBlogException;
}
