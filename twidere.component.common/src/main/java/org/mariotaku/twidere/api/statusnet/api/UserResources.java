package org.mariotaku.twidere.api.statusnet.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.PageableResponseList;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.User;

/**
 * Created by mariotaku on 16/3/4.
 */
public interface UserResources {

    @GET("/statuses/friends.json")
    PageableResponseList<User> getStatusesFriendsList(@Query("user_id") long userId, @Query Paging paging) throws TwitterException;

    @GET("/statuses/friends.json")
    PageableResponseList<User> getStatusesFriendsList(@Query("screen_name") String screenName, @Query Paging paging) throws TwitterException;

    @GET("/statuses/followers.json")
    PageableResponseList<User> getStatusesFollowersList(@Query("user_id") long userId, @Query Paging paging) throws TwitterException;

    @GET("/statuses/followers.json")
    PageableResponseList<User> getStatusesFollowersList(@Query("screen_name") String screenName, @Query Paging paging) throws TwitterException;

}
