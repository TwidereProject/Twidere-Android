package org.mariotaku.twidere.api.twitter.api;

import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.User;

public interface PrivateFriendsFollowersResources extends PrivateResources {

	public User acceptFriendship(long userId) throws TwitterException;

	public User acceptFriendship(String screenName) throws TwitterException;

	public User denyFriendship(long userId) throws TwitterException;

	public User denyFriendship(String screenName) throws TwitterException;

}
