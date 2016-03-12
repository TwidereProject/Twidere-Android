package org.mariotaku.twidere.api.fanfou.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.restfu.http.mime.Body;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;

/**
 * Created by mariotaku on 16/3/10.
 */
public interface PhotosResources {

    @GET("/photos/user_timeline.json")
    ResponseList<Status> getPhotosUserTimeline(@Query("id") String id, @Query Paging paging) throws TwitterException;

    @POST("/photos/upload.json")
    @BodyType(BodyType.MULTIPART)
    Status uploadPhoto(@Param("photo") Body data, @Param("status") String status,
                                    @Param("location") String location) throws TwitterException;

}
