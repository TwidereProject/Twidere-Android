package org.mariotaku.microblog.library.fanfou.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.User;

/**
 * Created by mariotaku on 16/3/10.
 */
public interface SearchResources {

    @GET("/search/public_timeline.json")
    ResponseList<Status> searchPublicTimeline(@Query("q") String query, @Query Paging paging) throws MicroBlogException;

    @GET("/search/users.json")
    ResponseList<User> searchFanfouUsers(@Query("q") String query, @Query Paging paging) throws MicroBlogException;

}
