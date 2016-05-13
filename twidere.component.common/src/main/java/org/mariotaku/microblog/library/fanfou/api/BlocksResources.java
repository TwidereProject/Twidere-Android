package org.mariotaku.microblog.library.fanfou.api;

import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.User;

/**
 * Created by mariotaku on 16/3/11.
 */
public interface BlocksResources {

    @POST("/blocks/create.json")
    User createFanfouBlock(@Param("id") String userId) throws MicroBlogException;

    @POST("/blocks/destroy.json")
    User destroyFanfouBlock(@Param("id") String userId) throws MicroBlogException;

    @POST("/blocks/blocking.json")
    ResponseList<User> getFanfouBlocking(@Param Paging paging) throws MicroBlogException;
}
