/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j.api;

import org.mariotaku.simplerestapi.http.BodyType;
import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.method.POST;
import org.mariotaku.simplerestapi.param.Body;
import org.mariotaku.simplerestapi.param.Form;
import org.mariotaku.simplerestapi.param.Query;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.TwitterException;

/**
 * @author Joern Huxhorn - jhuxhorn at googlemail.com
 */
public interface DirectMessagesResources {

    @POST("/direct_messages/destroy.json")
    @Body(BodyType.FORM)
    DirectMessage destroyDirectMessage(@Form("id") long id) throws TwitterException;

    @GET("/direct_messages.json")
    ResponseList<DirectMessage> getDirectMessages() throws TwitterException;

    @GET("/direct_messages.json")
    ResponseList<DirectMessage> getDirectMessages(@Query({"since_id", "max_id", "count"}) Paging paging) throws TwitterException;

    @GET("/direct_messages/sent.json")
    ResponseList<DirectMessage> getSentDirectMessages() throws TwitterException;

    @GET("/direct_messages/sent.json")
    ResponseList<DirectMessage> getSentDirectMessages(@Query({"since_id", "max_id", "count"}) Paging paging) throws TwitterException;

    @POST("/direct_messages/new.json")
    @Body(BodyType.FORM)
    DirectMessage sendDirectMessage(@Form("user_id") long userId, @Form("text") String text) throws TwitterException;

    @POST("/direct_messages/new.json")
    @Body(BodyType.FORM)
    DirectMessage sendDirectMessage(@Form("user_id") long userId, @Form("text") String text, @Form("media_id") long mediaId) throws TwitterException;

    @POST("/direct_messages/new.json")
    @Body(BodyType.FORM)
    DirectMessage sendDirectMessage(@Form("screen_name") String screenName, @Form("text") String text) throws TwitterException;

    @POST("/direct_messages/new.json")
    @Body(BodyType.FORM)
    DirectMessage sendDirectMessage(@Form("screen_name") String screenName, @Form("text") String text, @Form("media_id") long mediaId) throws TwitterException;

    DirectMessage showDirectMessage(long id) throws TwitterException;
}
