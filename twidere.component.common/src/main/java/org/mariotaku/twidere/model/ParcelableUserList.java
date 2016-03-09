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
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.model.util.UserKeyConverter;

@ParcelablePlease
@JsonObject
public class ParcelableUserList implements Parcelable, Comparable<ParcelableUserList> {

    @ParcelableThisPlease
    @JsonField(name = "account_id", typeConverter = UserKeyConverter.class)
    public UserKey account_key;
    @ParcelableThisPlease
    @JsonField(name = "id")
    public long id;
    @ParcelableThisPlease
    @JsonField(name = "is_public")
    public boolean is_public;
    @ParcelableThisPlease
    @JsonField(name = "is_following")
    public boolean is_following;
    @ParcelableThisPlease
    @JsonField(name = "description")
    public String description;
    @ParcelableThisPlease
    @JsonField(name = "name")
    public String name;

    @ParcelableThisPlease
    @JsonField(name = "position")
    public long position;

    @ParcelableThisPlease
    @JsonField(name = "members_count")
    public long members_count;
    @ParcelableThisPlease
    @JsonField(name = "subscribers_count")
    public long subscribers_count;

    @ParcelableThisPlease
    @JsonField(name = "user_id", typeConverter = UserKeyConverter.class)
    public UserKey user_key;
    @ParcelableThisPlease
    @JsonField(name = "user_screen_name")
    public String user_screen_name;
    @ParcelableThisPlease
    @JsonField(name = "user_name")
    public String user_name;
    @ParcelableThisPlease
    @JsonField(name = "user_profile_image_url")
    public String user_profile_image_url;

    public ParcelableUserList() {
    }

    @Override
    public int compareTo(@NonNull final ParcelableUserList another) {
        return (int) (position - another.position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableUserList that = (ParcelableUserList) o;

        if (id != that.id) return false;
        return account_key.equals(that.account_key);

    }

    @Override
    public int hashCode() {
        int result = account_key.hashCode();
        result = 31 * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableUserList{" +
                "members_count=" + members_count +
                ", subscribers_count=" + subscribers_count +
                ", account_key=" + account_key +
                ", id=" + id +
                ", user_id=" + user_key +
                ", position=" + position +
                ", is_public=" + is_public +
                ", is_following=" + is_following +
                ", description='" + description + '\'' +
                ", name='" + name + '\'' +
                ", user_screen_name='" + user_screen_name + '\'' +
                ", user_name='" + user_name + '\'' +
                ", user_profile_image_url='" + user_profile_image_url + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableUserListParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableUserList> CREATOR = new Creator<ParcelableUserList>() {
        public ParcelableUserList createFromParcel(Parcel source) {
            ParcelableUserList target = new ParcelableUserList();
            ParcelableUserListParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableUserList[] newArray(int size) {
            return new ParcelableUserList[size];
        }
    };
}
