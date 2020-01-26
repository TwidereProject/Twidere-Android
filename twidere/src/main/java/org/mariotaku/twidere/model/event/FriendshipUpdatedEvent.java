/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.model.event;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import org.mariotaku.twidere.model.ParcelableRelationship;
import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 14/12/7.
 */
public class FriendshipUpdatedEvent {

    @NonNull
    UserKey accountKey;
    @NonNull
    UserKey userKey;
    @NonNull
    ParcelableRelationship relationship;

    public FriendshipUpdatedEvent(@NonNull UserKey accountKey, @NonNull UserKey userKey, @NonNull ParcelableRelationship relationship) {
        this.accountKey = accountKey;
        this.userKey = userKey;
        this.relationship = relationship;
    }

    @NonNull
    public UserKey getAccountKey() {
        return accountKey;
    }

    @NonNull
    public UserKey getUserKey() {
        return userKey;
    }

    @NonNull
    public ParcelableRelationship getRelationship() {
        return relationship;
    }

    public boolean isAccount(String accountId, String accountHost) {
        return accountKey.check(accountId, accountHost);
    }

    public boolean isUser(String id) {
        return TextUtils.equals(userKey.getId(), id);
    }

    public boolean isAccount(UserKey accountKey) {
        return this.accountKey.equals(accountKey);
    }
}
