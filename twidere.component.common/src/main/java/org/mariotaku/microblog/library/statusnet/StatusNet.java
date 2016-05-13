package org.mariotaku.microblog.library.statusnet;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.microblog.library.statusnet.api.GroupResources;
import org.mariotaku.microblog.library.statusnet.api.SearchResources;
import org.mariotaku.microblog.library.statusnet.api.StatusNetResources;
import org.mariotaku.microblog.library.statusnet.api.UserResources;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.User;

/**
 * Created by mariotaku on 16/3/4.
 */
public interface StatusNet extends StatusNetResources, GroupResources, SearchResources, UserResources {

    @GET("/externalprofile/show.json")
    User showExternalProfile(@Query("profileurl") String profileUrl) throws MicroBlogException;

}
