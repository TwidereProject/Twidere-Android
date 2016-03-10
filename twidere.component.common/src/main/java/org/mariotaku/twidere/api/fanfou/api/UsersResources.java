package org.mariotaku.twidere.api.fanfou.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.PageableResponseList;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.User;

/**
 * Created by mariotaku on 16/3/10.
 */
public interface UsersResources {

    @GET("/users/followers.json")
    PageableResponseList<User> getUsersFollowers(@Query("id") String id, @Query Paging paging) throws TwitterException;

    @GET("/users/friends.json")
    PageableResponseList<User> getUsersFriends(@Query("id") String id, @Query Paging paging) throws TwitterException;

}
