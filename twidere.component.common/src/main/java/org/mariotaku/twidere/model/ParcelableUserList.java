/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.api.twitter.model.UserList;
import org.mariotaku.twidere.util.TwitterContentUtils;

@JsonObject
public class ParcelableUserList implements Parcelable, Comparable<ParcelableUserList> {

    public static final Parcelable.Creator<ParcelableUserList> CREATOR = new Parcelable.Creator<ParcelableUserList>() {
        @Override
        public ParcelableUserList createFromParcel(final Parcel in) {
            return new ParcelableUserList(in);
        }

        @Override
        public ParcelableUserList[] newArray(final int size) {
            return new ParcelableUserList[size];
        }
    };

    @JsonField(name = "members_count")
    public long members_count;
    @JsonField(name = "subscribers_count")
    public long subscribers_count;

    @JsonField(name = "account_id")
    public long account_id;
    @JsonField(name = "id")
    public long id;
    @JsonField(name = "user_id")
    public long user_id;
    @JsonField(name = "position")
    public long position;

    @JsonField(name = "is_public")
    public boolean is_public;
    @JsonField(name = "is_following")
    public boolean is_following;

    @JsonField(name = "description")
    public String description;
    @JsonField(name = "name")
    public String name;
    @JsonField(name = "user_screen_name")
    public String user_screen_name;
    @JsonField(name = "user_name")
    public String user_name;
    @JsonField(name = "user_profile_image_url")
    public String user_profile_image_url;

    public ParcelableUserList() {
    }

    public ParcelableUserList(final Parcel in) {
        position = in.readLong();
        account_id = in.readLong();
        id = in.readLong();
        is_public = in.readInt() == 1;
        is_following = in.readInt() == 1;
        name = in.readString();
        description = in.readString();
        user_id = in.readLong();
        user_name = in.readString();
        user_screen_name = in.readString();
        user_profile_image_url = in.readString();
        members_count = in.readLong();
        subscribers_count = in.readLong();
    }

    public ParcelableUserList(final UserList list, final long account_id) {
        this(list, account_id, 0);
    }

    public ParcelableUserList(final UserList list, final long account_id, final long position) {
        this(list, account_id, position, list.isFollowing());
    }

    public ParcelableUserList(final UserList list, final long account_id, final long position,
                              final boolean is_following) {
        final User user = list.getUser();
        this.position = position;
        this.account_id = account_id;
        id = list.getId();
        is_public = list.getMode() == UserList.Mode.PUBLIC;
        this.is_following = is_following;
        name = list.getName();
        description = list.getDescription();
        user_id = user.getId();
        user_name = user.getName();
        user_screen_name = user.getScreenName();
        user_profile_image_url = TwitterContentUtils.getProfileImageUrl(user);
        members_count = list.getMemberCount();
        subscribers_count = list.getSubscriberCount();
    }

    public static ParcelableUserList[] fromUserLists(UserList[] userLists, long accountId) {
        if (userLists == null) return null;
        int size = userLists.length;
        final ParcelableUserList[] result = new ParcelableUserList[size];
        for (int i = 0; i < size; i++) {
            result[i] = new ParcelableUserList(userLists[i], accountId);
        }
        return result;
    }

    @Override
    public int compareTo(@NonNull final ParcelableUserList another) {
        final long diff = position - another.position;
        if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) diff;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof ParcelableUserList)) return false;
        final ParcelableUserList other = (ParcelableUserList) obj;
        if (account_id != other.account_id) return false;
        if (id != other.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (account_id ^ account_id >>> 32);
        result = prime * result + (int) (id ^ id >>> 32);
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableUserList{members_count=" + members_count + ", subscribers_count=" + subscribers_count
                + ", account_id=" + account_id + ", id=" + id + ", user_id=" + user_id + ", position=" + position
                + ", is_public=" + is_public + ", is_following=" + is_following + ", description=" + description
                + ", name=" + name + ", user_screen_name=" + user_screen_name + ", user_name=" + user_name
                + ", user_profile_image_url=" + user_profile_image_url + "}";
    }

    @Override
    public void writeToParcel(final Parcel out, final int flags) {
        out.writeLong(position);
        out.writeLong(account_id);
        out.writeLong(id);
        out.writeInt(is_public ? 1 : 0);
        out.writeInt(is_following ? 1 : 0);
        out.writeString(name);
        out.writeString(description);
        out.writeLong(user_id);
        out.writeString(user_name);
        out.writeString(user_screen_name);
        out.writeString(user_profile_image_url);
        out.writeLong(members_count);
        out.writeLong(subscribers_count);
    }
}
