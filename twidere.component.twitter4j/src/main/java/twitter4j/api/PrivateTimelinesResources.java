package twitter4j.api;

import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.param.Query;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public interface PrivateTimelinesResources extends PrivateResources {

	@GET("/statuses/media_timeline.json")
	ResponseList<Status> getMediaTimeline() throws TwitterException;

	@GET("/statuses/media_timeline.json")
	ResponseList<Status> getMediaTimeline(@Query("user_id") long userId) throws TwitterException;

	@GET("/statuses/media_timeline.json")
	ResponseList<Status> getMediaTimeline(@Query("user_id") long userId, @Query({"since_id", "max_id", "count", "page"}) Paging paging) throws TwitterException;

	@GET("/statuses/media_timeline.json")
	ResponseList<Status> getMediaTimeline(@Query({"since_id", "max_id", "count", "page"}) Paging paging) throws TwitterException;

	@GET("/statuses/media_timeline.json")
	ResponseList<Status> getMediaTimeline(@Query("screen_name") String screenName) throws TwitterException;

	@GET("/statuses/media_timeline.json")
	ResponseList<Status> getMediaTimeline(@Query("screen_name") String screenName,@Query({"since_id", "max_id", "count", "page"})  Paging paging) throws TwitterException;
}
