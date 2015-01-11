/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package twitter4j;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.8
 */
public interface SiteStreamsListener extends StreamListener {
	/**
	 * @param forUser the user id to whom sent the event
	 * @param source
	 * @param blockedUser
	 */
	void onBlock(long forUser, User source, User blockedUser);

	void onDeletionNotice(long forUser, long directMessageId, long userId);

	void onDeletionNotice(long forUser, StatusDeletionNotice statusDeletionNotice);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param directMessage
	 */
	void onDirectMessage(long forUser, DirectMessage directMessage);

	/**
	 * callback method for {@link twitter4j.StreamController#removeUsers(long[])}
	 */
	void onDisconnectionNotice(String line);

	@Override
	void onException(Exception ex);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param source
	 * @param target
	 * @param favoritedStatus
	 */
	void onFavorite(long forUser, User source, User target, Status favoritedStatus);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param source
	 * @param followedUser
	 */
	void onFollow(long forUser, User source, User followedUser);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param friendIds
	 */
	void onFriendList(long forUser, long[] friendIds);

	void onStatus(long forUser, Status status);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param source
	 * @param unblockedUser
	 */
	void onUnblock(long forUser, User source, User unblockedUser);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param target
	 * @param unfavoritedStatus
	 */
	void onUnfavorite(long forUser, User source, User target, Status unfavoritedStatus);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param source
	 * @param unfollowedUser
	 * @since Twitter4J 2.1.11
	 */
	void onUnfollow(long forUser, User source, User unfollowedUser);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param listOwner
	 * @param list
	 */
	void onUserListCreation(long forUser, User listOwner, UserList list);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param listOwner
	 * @param list
	 */
	void onUserListDeletion(long forUser, User listOwner, UserList list);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param addedMember
	 * @param listOwner
	 * @param list
	 */
	void onUserListMemberAddition(long forUser, User addedMember, User listOwner, UserList list);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param deletedMember
	 * @param listOwner
	 * @param list
	 */
	void onUserListMemberDeletion(long forUser, User deletedMember, User listOwner, UserList list);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param subscriber
	 * @param listOwner
	 * @param list
	 */
	void onUserListSubscription(long forUser, User subscriber, User listOwner, UserList list);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param subscriber
	 * @param listOwner
	 * @param list
	 */
	void onUserListUnsubscription(long forUser, User subscriber, User listOwner, UserList list);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param listOwner
	 * @param list
	 */
	void onUserListUpdate(long forUser, User listOwner, UserList list);

	/**
	 * @param forUser the user id to whom sent the event
	 * @param updatedUser updated user
	 * @since Twitter4J 2.1.9
	 */
	void onUserProfileUpdate(long forUser, User updatedUser);
}
