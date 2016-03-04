package org.mariotaku.twidere.api.statusnet.api;

import org.mariotaku.restfu.annotation.param.Query;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;

/**
 * Created by mariotaku on 16/3/4.
 */
public interface GroupResources {

    ResponseList<Status> getUserListStatuses(@Query("group_id") long groupId, @Query Paging paging) throws TwitterException;

    ResponseList<Status> getGroupStatuses(@Query("id") String name, @Query Paging paging) throws TwitterException;

}
