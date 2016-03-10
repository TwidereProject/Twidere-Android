package org.mariotaku.twidere.api.fanfou.api;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Queries;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.twidere.api.fanfou.model.FanfouSearchStatus;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.User;

import java.util.List;

/**
 * Created by mariotaku on 16/3/10.
 */
public interface SearchResources {

    @GET("/search/public_timeline.json")
    @Queries(@KeyValue(key = "format", value = "html"))
    ResponseList<FanfouSearchStatus> searchPublicTimeline(@Query("q") String query, @Query Paging paging) throws TwitterException;

    @GET("/search/users.json")
    ResponseList<User> searchFanfouUsers(@Query("q") String query, @Query Paging paging) throws TwitterException;

}
