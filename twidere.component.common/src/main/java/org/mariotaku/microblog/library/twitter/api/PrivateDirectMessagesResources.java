/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.microblog.library.twitter.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.restfu.annotation.param.Queries;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.ConversationTimeline;
import org.mariotaku.microblog.library.twitter.model.NewDm;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseCode;
import org.mariotaku.microblog.library.twitter.model.UserInbox;


@Queries(@KeyValue(key = "include_groups", value = "true"))
public interface PrivateDirectMessagesResources extends PrivateResources {

    @POST("/dm/conversation/{conversation_id}/delete.json")
    @BodyType(BodyType.FORM)
    ResponseCode destroyDirectMessagesConversation(@Path("conversation_id") String conversationId) throws MicroBlogException;

    @POST("/dm/new.json")
    ResponseCode sendDm(@Param NewDm newDm) throws MicroBlogException;

    @POST("/dm/conversation/{account_id}-{user_id}/delete.json")
    @BodyType(BodyType.FORM)
    ResponseCode destroyDirectMessagesConversation(@Path("account_id") String accountId, @Path("user_id") String userId) throws MicroBlogException;

    @GET("/dm/user_inbox.json")
    UserInbox getUserInbox(@Query Paging paging) throws MicroBlogException;

    @GET("/dm/conversation/{conversation_id}.json")
    ConversationTimeline getUserInbox(@Path("conversation_id") String conversationId, @Query Paging paging) throws MicroBlogException;
}
