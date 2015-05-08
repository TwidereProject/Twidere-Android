package org.mariotaku.twidere.api.twitter.api;

import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.param.Query;

import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.model.StatusActivitySummary;
import org.mariotaku.twidere.api.twitter.model.TranslationResult;
import org.mariotaku.twidere.api.twitter.TwitterException;

public interface PrivateTweetResources extends PrivateResources {

    StatusActivitySummary getStatusActivitySummary(@Query("id") long statusId) throws TwitterException;

    StatusActivitySummary getStatusActivitySummary(long statusId, boolean includeUserEntities) throws TwitterException;

    @GET("/conversation/show.json")
    ResponseList<Status> showConversation(@Query("id") long statusId) throws TwitterException;

    @GET("/conversation/show.json")
    ResponseList<Status> showConversation(@Query("id") long statusId, @Query Paging paging) throws TwitterException;

    @GET("/translations/show.json")
    TranslationResult showTranslation(@Query("id") long statusId, @Query("dest") String dest) throws TwitterException;
}
