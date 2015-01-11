package twitter4j;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.8
 */
public class SiteStreamsAdapter implements SiteStreamsListener {
	@Override
	public void onBlock(final long forUser, final User source, final User blockedUser) {
	}

	@Override
	public void onDeletionNotice(final long forUser, final long directMessageId, final long userId) {
	}

	@Override
	public void onDeletionNotice(final long forUser, final StatusDeletionNotice statusDeletionNotice) {
	}

	@Override
	public void onDirectMessage(final long forUser, final DirectMessage directMessage) {
	}

	@Override
	public void onDisconnectionNotice(final String screenName) {
	}

	@Override
	public void onException(final Exception ex) {
	}

	@Override
	public void onFavorite(final long forUser, final User source, final User target, final Status favoritedStatus) {
	}

	@Override
	public void onFollow(final long forUser, final User source, final User followedUser) {
	}

	@Override
	public void onFriendList(final long forUser, final long[] friendIds) {
	}

	@Override
	public void onStatus(final long forUser, final Status status) {
	}

	@Override
	public void onUnblock(final long forUser, final User source, final User unblockedUser) {
	}

	@Override
	public void onUnfavorite(final long forUser, final User source, final User target, final Status unfavoritedStatus) {
	}

	@Override
	public void onUnfollow(final long forUser, final User source, final User followedUser) {
	}

	@Override
	public void onUserListCreation(final long forUser, final User listOwner, final UserList list) {
	}

	@Override
	public void onUserListDeletion(final long forUser, final User listOwner, final UserList list) {
	}

	@Override
	public void onUserListMemberAddition(final long forUser, final User addedUser, final User listOwner,
			final UserList list) {
	}

	@Override
	public void onUserListMemberDeletion(final long forUser, final User deletedUser, final User listOwner,
			final UserList list) {
	}

	@Override
	public void onUserListSubscription(final long forUser, final User subscriber, final User listOwner,
			final UserList list) {
	}

	@Override
	public void onUserListUnsubscription(final long forUser, final User subscriber, final User listOwner,
			final UserList list) {
	}

	@Override
	public void onUserListUpdate(final long forUser, final User listOwner, final UserList list) {
	}

	@Override
	public void onUserProfileUpdate(final long forUser, final User updatedUser) {
	}
}
