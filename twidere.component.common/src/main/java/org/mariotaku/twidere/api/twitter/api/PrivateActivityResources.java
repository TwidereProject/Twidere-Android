package org.mariotaku.twidere.api.twitter.api;

import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.TwitterException;

public interface PrivateActivityResources extends PrivateResources {
	public ResponseList<Activity> getActivitiesAboutMe() throws TwitterException;

	public ResponseList<Activity> getActivitiesAboutMe(Paging paging) throws TwitterException;

	public ResponseList<Activity> getActivitiesByFriends() throws TwitterException;

	public ResponseList<Activity> getActivitiesByFriends(Paging paging) throws TwitterException;
}
