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

package org.mariotaku.twidere.api.twitter.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Body;
import org.mariotaku.restfu.annotation.param.Form;
import org.mariotaku.restfu.annotation.param.MethodExtra;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.DirectMessage;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;

@SuppressWarnings("RedundantThrows")
@MethodExtra(name = "extra_params", values = {"full_text", "include_entities", "include_cards", "cards_platform"})
public interface DirectMessagesResources {

    @POST("/direct_messages/destroy.json")
    @Body(BodyType.FORM)
    DirectMessage destroyDirectMessage(@Form("id") long id) throws TwitterException;

    @GET("/direct_messages.json")
    ResponseList<DirectMessage> getDirectMessages(@Query Paging paging) throws TwitterException;

    @GET("/direct_messages/sent.json")
    ResponseList<DirectMessage> getSentDirectMessages(@Query Paging paging) throws TwitterException;

    @POST("/direct_messages/new.json")
    @Body(BodyType.FORM)
    DirectMessage sendDirectMessage(@Form("user_id") long userId, @Form("text") String text)
            throws TwitterException;

    @POST("/direct_messages/new.json")
    @Body(BodyType.FORM)
    DirectMessage sendDirectMessage(@Form("user_id") long userId, @Form("text") String text,
                                    @Form("media_id") long mediaId) throws TwitterException;

    @POST("/direct_messages/new.json")
    @Body(BodyType.FORM)
    DirectMessage sendDirectMessage(@Form("screen_name") String screenName, @Form("text") String text)
            throws TwitterException;

    @POST("/direct_messages/new.json")
    @Body(BodyType.FORM)
    DirectMessage sendDirectMessage(@Form("screen_name") String screenName, @Form("text") String text,
                                    @Form("media_id") long mediaId) throws TwitterException;

    @GET("/direct_messages/show.json")
    DirectMessage showDirectMessage(@Query("id") long id) throws TwitterException;
}
