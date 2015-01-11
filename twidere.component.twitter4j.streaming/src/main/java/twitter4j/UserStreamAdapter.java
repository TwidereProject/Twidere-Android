/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package twitter4j;

/**
 * @author yusuke at mac.com
 * @since Twitter4J 2.1.3
 */
public class UserStreamAdapter extends StatusAdapter implements UserStreamListener {
	@Override
	public void onBlock(final User source, final User blockedUser) {
	}

	@Override
	public void onDeletionNotice(final long directMessageId, final long userId) {
	}

	@Override
	public void onDirectMessage(final DirectMessage directMessage) {
	}

	@Override
	public void onException(final Exception ex) {
	}

	@Override
	public void onFavorite(final User source, final User target, final Status favoritedStatus) {
	}

	@Override
	public void onFollow(final User source, final User followedUser) {
	}

	@Override
	public void onFriendList(final long[] friendIds) {
	}

	@Override
	public void onUnblock(final User source, final User unblockedUser) {
	}

	@Override
	public void onUnfavorite(final User source, final User target, final Status unfavoritedStatus) {
	}

	@Override
	public void onUserListCreation(final User listOwner, final UserList list) {
	}

	@Override
	public void onUserListDeletion(final User listOwner, final UserList list) {
	}

	@Override
	public void onUserListMemberAddition(final User addedMember, final User listOwner, final UserList list) {
	}

	@Override
	public void onUserListMemberDeletion(final User deletedMember, final User listOwner, final UserList list) {
	}

	@Override
	public void onUserListSubscription(final User subscriber, final User listOwner, final UserList list) {
	}

	@Override
	public void onUserListUnsubscription(final User subscriber, final User listOwner, final UserList list) {
	}

	@Override
	public void onUserListUpdate(final User listOwner, final UserList list) {
	}

	@Override
	public void onUserProfileUpdate(final User updatedUser) {
	}
}
