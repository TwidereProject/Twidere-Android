package twitter4j.api;

import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.param.Query;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusActivitySummary;
import twitter4j.TranslationResult;
import twitter4j.TwitterException;

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
