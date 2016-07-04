package org.mariotaku.microblog.library.statusnet.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.microblog.library.statusnet.model.StatusNetConfig;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.Status;

/**
 * Created by mariotaku on 16/2/27.
 */
public interface StatusNetResources {

    @GET("/statusnet/config.json")
    StatusNetConfig getStatusNetConfig() throws MicroBlogException;

    @GET("/statusnet/conversation/{id}.json")
    ResponseList<Status> getStatusNetConversation(@Path("id") String statusId, @Query Paging paging) throws MicroBlogException;

}
