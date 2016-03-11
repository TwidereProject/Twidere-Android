package org.mariotaku.twidere.api.fanfou.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.User;

/**
 * Created by mariotaku on 16/3/11.
 */
public interface FriendshipsResources {

    @POST("/friendships/create.json")
    User createFanfouFriendship(@Param("id") String id) throws TwitterException;

    @POST("/friendships/destroy.json")
    User destroyFanfouFriendship(@Param("id") String id) throws TwitterException;

    @POST("/friendships/accept.json")
    User acceptFanfouFriendship(@Param("id") String id) throws TwitterException;

    @POST("/friendships/deny.json")
    User denyFanfouFriendship(@Param("id") String id) throws TwitterException;

    @GET("/friendships/requests.json")
    ResponseList<User> getFriendshipsRequests(@Query Paging paging) throws TwitterException;

}
