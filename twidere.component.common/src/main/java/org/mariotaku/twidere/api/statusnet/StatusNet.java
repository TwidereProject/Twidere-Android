package org.mariotaku.twidere.api.statusnet;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.twidere.api.statusnet.api.GroupResources;
import org.mariotaku.twidere.api.statusnet.api.SearchResources;
import org.mariotaku.twidere.api.statusnet.api.StatusNetResources;
import org.mariotaku.twidere.api.statusnet.api.UserResources;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.User;

/**
 * Created by mariotaku on 16/3/4.
 */
public interface StatusNet extends StatusNetResources, GroupResources, SearchResources, UserResources {

    @GET("/externalprofile/show.json")
    User showExternalProfile(@Query("profileurl") String profileUrl) throws TwitterException;

}
