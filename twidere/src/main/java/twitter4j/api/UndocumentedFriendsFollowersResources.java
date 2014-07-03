package twitter4j.api;

import twitter4j.TwitterException;
import twitter4j.User;

public interface UndocumentedFriendsFollowersResources extends UndocumentedResources {

	public User acceptFriendship(long userId) throws TwitterException;

	public User acceptFriendship(String screenName) throws TwitterException;

	public User denyFriendship(long userId) throws TwitterException;

	public User denyFriendship(String screenName) throws TwitterException;

}
