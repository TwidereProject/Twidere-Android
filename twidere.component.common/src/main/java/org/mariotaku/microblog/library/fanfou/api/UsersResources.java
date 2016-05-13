package org.mariotaku.microblog.library.fanfou.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.PageableResponseList;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.User;

/**
 * Created by mariotaku on 16/3/10.
 */
public interface UsersResources {

    @GET("/users/show.json")
    User showFanfouUser(@Query("id") String userId) throws MicroBlogException;

    @GET("/users/followers.json")
    PageableResponseList<User> getUsersFollowers(@Query("id") String id, @Query Paging paging) throws MicroBlogException;

    @GET("/users/friends.json")
    PageableResponseList<User> getUsersFriends(@Query("id") String id, @Query Paging paging) throws MicroBlogException;

}
