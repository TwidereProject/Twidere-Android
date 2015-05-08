package org.mariotaku.twidere.api.twitter.api;

import org.mariotaku.simplerestapi.method.GET;
import org.mariotaku.simplerestapi.param.Query;

import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.Status;
import org.mariotaku.twidere.api.twitter.TwitterException;

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
