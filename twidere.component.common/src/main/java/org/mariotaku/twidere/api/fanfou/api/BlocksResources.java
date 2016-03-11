package org.mariotaku.twidere.api.fanfou.api;

import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.http.BodyType;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.User;

/**
 * Created by mariotaku on 16/3/11.
 */
public interface BlocksResources {

    @POST("/blocks/create.json")
    User createFanfouBlock(@Param("id") String userId) throws TwitterException;

    @POST("/blocks/destroy.json")
    User destroyFanfouBlock(@Param("id") String userId) throws TwitterException;
}
