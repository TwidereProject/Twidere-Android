/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.microblog.library.twitter.callback;

import android.support.annotation.NonNull;

import org.mariotaku.microblog.library.twitter.model.DeletionEvent;
import org.mariotaku.microblog.library.twitter.model.DirectMessage;
import org.mariotaku.microblog.library.twitter.model.Status;
import org.mariotaku.microblog.library.twitter.model.TwitterStreamObject;
import org.mariotaku.microblog.library.twitter.model.User;
import org.mariotaku.microblog.library.twitter.model.UserList;
import org.mariotaku.microblog.library.twitter.model.Warning;

import java.io.IOException;
import java.util.Date;

/**
 * Created by mariotaku on 2017/3/11.
 */

public abstract class SimpleUserStreamCallback extends UserStreamCallback {
    @Override
    protected boolean onConnected() {
        return false;
    }

    @Override
    protected boolean onDisconnectNotice(final int code, final String reason) {
        return false;
    }

    @Override
    protected boolean onException(@NonNull final Throwable ex) {
        return false;
    }

    @Override
    protected boolean onStatus(@NonNull final Status status) {
        return false;
    }

    @Override
    protected boolean onDirectMessage(@NonNull final DirectMessage directMessage) {
        return false;
    }

    @Override
    protected boolean onBlock(final Date createdAt, final User source, final User blockedUser) {
        return false;
    }

    @Override
    protected boolean onDirectMessageDeleted(@NonNull final DeletionEvent event) {
        return false;
    }

    @Override
    protected boolean onStatusDeleted(@NonNull final DeletionEvent event) {
        return false;
    }

    @Override
    protected boolean onFavorite(@NonNull final Date createdAt, @NonNull final User source, @NonNull final User target, @NonNull final Status targetObject) {
        return false;
    }

    @Override
    protected boolean onFollow(@NonNull final Date createdAt, @NonNull final User source, @NonNull final User target) {
        return false;
    }

    @Override
    protected boolean onUnfollow(@NonNull final Date createdAt, @NonNull final User source, @NonNull final User target) {
        return false;
    }

    @Override
    protected boolean onFriendList(@NonNull final String[] friendIds) {
        return false;
    }

    @Override
    protected boolean onScrubGeo(final String userId, final String upToStatusId) {
        return false;
    }

    @Override
    protected boolean onStallWarning(final Warning warn) {
        return false;
    }

    @Override
    protected boolean onTrackLimitationNotice(final int numberOfLimitedStatuses) {
        return false;
    }

    @Override
    protected boolean onUnblock(@NonNull final Date createdAt, @NonNull final User source, @NonNull final User unblockedUser) {
        return false;
    }

    @Override
    protected boolean onUnfavorite(@NonNull final User source, @NonNull final User target, @NonNull final Status targetStatus) {
        return false;
    }

    @Override
    protected boolean onUserListCreation(@NonNull final Date createdAt, @NonNull final User source, @NonNull final UserList targetObject) {
        return false;
    }

    @Override
    protected boolean onUserListDeletion(@NonNull final Date createdAt, @NonNull final User source, @NonNull final UserList targetObject) {
        return false;
    }

    @Override
    protected boolean onUserListMemberAddition(@NonNull final Date createdAt, @NonNull final User source, @NonNull final User target, @NonNull final UserList targetObject) {
        return false;
    }

    @Override
    protected boolean onUserListMemberDeletion(@NonNull final Date createdAt, @NonNull final User source, @NonNull final User target, @NonNull final UserList targetObject) {
        return false;
    }

    @Override
    protected boolean onUserListSubscription(@NonNull final Date createdAt, @NonNull final User source, @NonNull final User target, @NonNull final UserList targetObject) {
        return false;
    }

    @Override
    protected boolean onUserListUnsubscription(@NonNull final Date createdAt, @NonNull final User source, @NonNull final User target, @NonNull final UserList targetObject) {
        return false;
    }

    @Override
    protected boolean onUserListUpdate(@NonNull final Date createdAt, @NonNull final User source, @NonNull final UserList targetObject) {
        return false;
    }

    @Override
    protected boolean onUserProfileUpdate(@NonNull final Date createdAt, @NonNull final User updatedUser) {
        return false;
    }

    @Override
    protected boolean onQuotedTweet(@NonNull final Date createdAt, @NonNull final User source, @NonNull final User target, @NonNull final Status targetObject) {
        return false;
    }

    @Override
    protected boolean onFavoritedRetweet(@NonNull final Date createdAt, @NonNull final User source, @NonNull final User target, @NonNull final Status targetObject) {
        return false;
    }

    @Override
    protected boolean onRetweetedRetweet(@NonNull final Date createdAt, @NonNull final User source, @NonNull final User target, @NonNull final Status targetObject) {
        return false;
    }

    @Override
    protected void onUnhandledEvent(@NonNull final TwitterStreamObject obj, @NonNull final String json) throws IOException {

    }
}
