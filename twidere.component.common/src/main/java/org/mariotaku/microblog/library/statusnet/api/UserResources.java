package org.mariotaku.microblog.library.statusnet.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.PageableResponseList;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.User;

/**
 * Created by mariotaku on 16/3/4.
 */
public interface UserResources {

    @GET("/statuses/friends.json")
    PageableResponseList<User> getStatusesFriendsList(@Query("user_id") String userId, @Query Paging paging) throws MicroBlogException;

    @GET("/statuses/friends.json")
    PageableResponseList<User> getStatusesFriendsListByScreenName(@Query("screen_name") String screenName, @Query Paging paging) throws MicroBlogException;

    @GET("/statuses/followers.json")
    PageableResponseList<User> getStatusesFollowersList(@Query("user_id") String userId, @Query Paging paging) throws MicroBlogException;

    @GET("/statuses/followers.json")
    PageableResponseList<User> getStatusesFollowersListByScreenName(@Query("screen_name") String screenName, @Query Paging paging) throws MicroBlogException;

}
