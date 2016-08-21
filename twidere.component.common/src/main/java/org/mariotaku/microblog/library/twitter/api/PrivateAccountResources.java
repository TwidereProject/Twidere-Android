package org.mariotaku.microblog.library.twitter.api;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.PinTweetResult;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Param;

/**
 * Created by mariotaku on 16/8/20.
 */
public interface PrivateAccountResources extends PrivateResources {

    @POST("/account/pin_tweet.json")
    PinTweetResult pinTweet(@Param("id") String id) throws MicroBlogException;

    @POST("/account/unpin_tweet.json")
    PinTweetResult unpinTweet(@Param("id") String id) throws MicroBlogException;

}
