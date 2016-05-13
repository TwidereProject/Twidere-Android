package org.mariotaku.microblog.library.statusnet.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.Status;

/**
 * Created by mariotaku on 16/3/4.
 */
public interface SearchResources {

    @GET("/search.json")
    ResponseList<Status> searchStatuses(@Query("q") String query, @Query Paging paging) throws MicroBlogException;

}
