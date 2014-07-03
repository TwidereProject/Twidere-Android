package twitter4j.api;

import twitter4j.Activity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.TwitterException;

public interface UndocumentedActivityResources extends UndocumentedResources {
	public ResponseList<Activity> getActivitiesAboutMe() throws TwitterException;

	public ResponseList<Activity> getActivitiesAboutMe(Paging paging) throws TwitterException;

	public ResponseList<Activity> getActivitiesByFriends() throws TwitterException;

	public ResponseList<Activity> getActivitiesByFriends(Paging paging) throws TwitterException;
}
