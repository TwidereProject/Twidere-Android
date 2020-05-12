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
import org.mariotaku.microblog.library.twitter.model.DirectMessage;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.template.DirectMessageAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.annotation.param.Query;

@SuppressWarnings("RedundantThrows")
@Params(template = DirectMessageAnnotationTemplate.class)
public interface DirectMessagesResources {

    @GET("/direct_messages.json")
    ResponseList<DirectMessage> getDirectMessages(@Query Paging paging) throws MicroBlogException;

    @GET("/direct_messages/sent.json")
    ResponseList<DirectMessage> getSentDirectMessages(@Query Paging paging) throws MicroBlogException;

    @POST("/direct_messages/new.json")
    DirectMessage sendDirectMessage(@Param("user_id") String userId, @Param("text") String text)
            throws MicroBlogException;

    @POST("/direct_messages/new.json")
    DirectMessage sendDirectMessage(@Param("user_id") String userId, @Param("text") String text,
            @Param("media_id") String mediaId) throws MicroBlogException;

    @POST("/direct_messages/new.json")
    DirectMessage sendDirectMessageToScreenName(@Param("screen_name") String screenName, @Param("text") String text)
            throws MicroBlogException;

    @POST("/direct_messages/new.json")
    DirectMessage sendDirectMessageToScreenName(@Param("screen_name") String screenName, @Param("text") String text,
            @Param("media_id") String mediaId) throws MicroBlogException;

}
