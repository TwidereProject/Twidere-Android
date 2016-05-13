package org.mariotaku.microblog.library.fanfou.api;

import org.mariotaku.restfu.annotation.method.POST;
import org.mariotaku.restfu.annotation.param.Path;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Status;

/**
 * Created by mariotaku on 16/3/11.
 */
public interface FavoritesResources {

    @POST("/favorites/create/{id}.json")
    Status createFanfouFavorite(@Path("id") String id) throws MicroBlogException;

    @POST("/favorites/destroy/{id}.json")
    Status destroyFanfouFavorite(@Path("id") String id) throws MicroBlogException;
}
