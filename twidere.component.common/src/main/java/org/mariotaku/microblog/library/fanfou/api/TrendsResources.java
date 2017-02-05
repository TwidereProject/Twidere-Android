package org.mariotaku.microblog.library.fanfou.api;

import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.Trends;
import org.mariotaku.restfu.annotation.method.GET;

/**
 * Created by mariotaku on 2017/2/5.
 */

@SuppressWarnings("RedundantThrows")
public interface TrendsResources {

    @GET("/trends/list.json")
    Trends getFanfouTrends() throws MicroBlogException;
}
