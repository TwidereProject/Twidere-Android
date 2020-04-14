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

import androidx.annotation.Nullable;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.ConversationTimeline;
import org.mariotaku.microblog.library.twitter.model.DMResponse;
import org.mariotaku.microblog.library.twitter.model.NewDm;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseCode;
import org.mariotaku.microblog.library.twitter.model.UserEvents;
import org.mariotaku.microblog.library.twitter.model.UserInbox;
import org.mariotaku.microblog.library.twitter.template.DMAnnotationTemplate;
import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Params;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;

@Params(template = DMAnnotationTemplate.class)
public interface PrivateDirectMessagesResources extends PrivateResources {

    @POST("/dm/conversation/{conversation_id}/delete.json")
    @BodyType(BodyType.FORM)
    ResponseCode deleteDmConversation(@Path("conversation_id") String conversationId)
            throws MicroBlogException;

    @POST("/dm/conversation/{conversation_id}/mark_read.json")
    @BodyType(BodyType.FORM)
    ResponseCode markDmRead(@Path("conversation_id") String conversationId,
            @Param("last_read_event_id") String lastReadEventId) throws MicroBlogException;

    @POST("/dm/update_last_seen_event_id.json")
    @BodyType(BodyType.FORM)
    ResponseCode updateLastSeenEventId(@Param("last_seen_event_id") String lastSeenEventId) throws MicroBlogException;

    @POST("/dm/conversation/{conversation_id}/update_name.json")
    @BodyType(BodyType.FORM)
    ResponseCode updateDmConversationName(@Path("conversation_id") String conversationId,
            @Param("name") String name) throws MicroBlogException;

    /**
     * Update DM conversation avatar
     *
     * @param conversationId DM conversation ID
     * @param avatarId       Avatar media ID, null for removing avatar
     * @return HTTP response code
     */
    @POST("/dm/conversation/{conversation_id}/update_avatar.json")
    @BodyType(BodyType.FORM)
    ResponseCode updateDmConversationAvatar(@Path("conversation_id") String conversationId,
            @Param(value = "avatar_id", ignoreOnNull = true) @Nullable String avatarId) throws MicroBlogException;

    @POST("/dm/conversation/{conversation_id}/disable_notifications.json")
    ResponseCode disableDmConversations(@Path("conversation_id") String conversationId)
            throws MicroBlogException;

    @POST("/dm/conversation/{conversation_id}/enable_notifications.json")
    ResponseCode enableDmConversations(@Path("conversation_id") String conversationId)
            throws MicroBlogException;

    @POST("/dm/new.json")
    DMResponse sendDm(@Param NewDm newDm) throws MicroBlogException;

    @POST("/dm/conversation/{conversation_id}/add_participants.json")
    DMResponse addParticipants(@Path("conversation_id") String conversationId,
            @Param(value = "participant_ids", arrayDelimiter = ',') String[] participantIds)
            throws MicroBlogException;

    @POST("/dm/destroy.json")
    ResponseCode destroyDm(@Param("dm_id") String id) throws MicroBlogException;

    @GET("/dm/user_inbox.json")
    UserInbox getUserInbox(@Query Paging paging) throws MicroBlogException;

    @GET("/dm/user_updates.json")
    UserEvents getUserUpdates(@Query("cursor") String cursor) throws MicroBlogException;

    @GET("/dm/conversation/{conversation_id}.json")
    ConversationTimeline getDmConversation(@Path("conversation_id") String conversationId,
            @Query Paging paging) throws MicroBlogException;
}
