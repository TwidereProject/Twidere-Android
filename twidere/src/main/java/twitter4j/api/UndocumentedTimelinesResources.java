package twitter4j.api;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public interface UndocumentedTimelinesResources extends UndocumentedResources {

	ResponseList<Status> getMediaTimeline() throws TwitterException;

	ResponseList<Status> getMediaTimeline(long userId) throws TwitterException;

	ResponseList<Status> getMediaTimeline(long userId, Paging paging) throws TwitterException;

	ResponseList<Status> getMediaTimeline(Paging paging) throws TwitterException;

	ResponseList<Status> getMediaTimeline(String screenName) throws TwitterException;

	ResponseList<Status> getMediaTimeline(String screenName, Paging paging) throws TwitterException;
}
