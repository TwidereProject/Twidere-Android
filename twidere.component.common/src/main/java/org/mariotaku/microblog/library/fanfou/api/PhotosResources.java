package org.mariotaku.microblog.library.fanfou.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.microblog.library.fanfou.model.PhotoStatusUpdate;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Paging;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.Status;

/**
 * Created by mariotaku on 16/3/10.
 */
public interface PhotosResources {

    @GET("/photos/user_timeline.json")
    ResponseList<Status> getPhotosUserTimeline(@Query("id") String id, @Query Paging paging) throws MicroBlogException;

    @POST("/photos/upload.json")
    @BodyType(BodyType.MULTIPART)
    Status uploadPhoto(@Param PhotoStatusUpdate update) throws MicroBlogException;

}
