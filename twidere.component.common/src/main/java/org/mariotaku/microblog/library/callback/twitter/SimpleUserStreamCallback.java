/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.microblog.library.callback.twitter;

import android.support.annotation.NonNull;

import org.mariotaku.microblog.library.model.microblog.DeletionEvent;
import org.mariotaku.microblog.library.model.microblog.DirectMessage;
import org.mariotaku.microblog.library.model.microblog.Status;
import org.mariotaku.microblog.library.model.microblog.TwitterStreamObject;
import org.mariotaku.microblog.library.model.microblog.User;
import org.mariotaku.microblog.library.model.microblog.UserList;
import org.mariotaku.microblog.library.model.microblog.Warning;

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
