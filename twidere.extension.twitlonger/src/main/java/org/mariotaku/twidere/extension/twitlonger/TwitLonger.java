package org.mariotaku.twidere.extension.twitlonger;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.method.PUT;
import org.mariotaku.restfu.annotation.param.Headers;
import org.mariotaku.restfu.annotation.param.KeyValue;
import org.mariotaku.restfu.annotation.param.Param;
import org.mariotaku.restfu.annotation.param.Path;

/**
 * Created by mariotaku on 16/2/20.
 */
@Headers({@KeyValue(key = "X-API-KEY", valueKey = "tl_api_key")})
public interface TwitLonger {

    @GET("/2/posts/{post_id}")
    Post getPost(@Path("post_id") String postId) throws TwitLongerException;

    @POST("/2/posts")
    @Headers({@KeyValue(key = "X-API-KEY", valueKey = "tl_api_key"),
            @KeyValue(key = "X-Auth-Service-Provider", value = "https://api.twitter.com/1.1/account/verify_credentials.json"),
            @KeyValue(key = "X-Verify-Credentials-Authorization", valueKey = "oauth_echo_authorization")})
    Post createPost(@Param NewPost newPost) throws TwitLongerException;

    @PUT("/2/posts/{post_id}")
    @Headers({@KeyValue(key = "X-API-KEY", valueKey = "tl_api_key"),
            @KeyValue(key = "X-Auth-Service-Provider", value = "https://api.twitter.com/1.1/account/verify_credentials.json"),
            @KeyValue(key = "X-Verify-Credentials-Authorization", valueKey = "oauth_echo_authorization")})
    Post updatePost(@Path("post_id") String postId, @Param("twitter_status_id") long twitterStatusId)
            throws TwitLongerException;

}
