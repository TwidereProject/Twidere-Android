package twitter4j.api;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusActivitySummary;
import twitter4j.TranslationResult;
import twitter4j.TwitterException;

public interface UndocumentedTweetResources extends UndocumentedResources {

	StatusActivitySummary getStatusActivitySummary(long statusId) throws TwitterException;

	StatusActivitySummary getStatusActivitySummary(long statusId, boolean includeUserEntities) throws TwitterException;

	ResponseList<Status> showConversation(long statusId) throws TwitterException;

	ResponseList<Status> showConversation(long statusId, Paging paging) throws TwitterException;

	TranslationResult showTranslation(long statusId, String dest) throws TwitterException;
}
