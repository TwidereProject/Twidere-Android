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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.api.twitter.model.UserMentionEntity;

@JsonObject
@ParcelablePlease(allFields = false)
public class ParcelableUserMention implements Parcelable {

    public static final Parcelable.Creator<ParcelableUserMention> CREATOR = new Parcelable.Creator<ParcelableUserMention>() {
        @Override
        public ParcelableUserMention createFromParcel(final Parcel in) {
            return new ParcelableUserMention(in);
        }

        @Override
        public ParcelableUserMention[] newArray(final int size) {
            return new ParcelableUserMention[size];
        }
    };

    @ParcelableThisPlease
    @JsonField(name = "id")
    public long id;
    @ParcelableThisPlease
    @JsonField(name = "name")
    public String name;
    @ParcelableThisPlease
    @JsonField(name = "screen_name")
    public String screen_name;

    public ParcelableUserMention() {

    }

    public ParcelableUserMention(final Parcel in) {
        id = in.readLong();
        name = in.readString();
        screen_name = in.readString();
    }

    public ParcelableUserMention(final UserMentionEntity entity) {
        id = entity.getId();
        name = entity.getName();
        screen_name = entity.getScreenName();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof ParcelableUserMention)) return false;
        final ParcelableUserMention other = (ParcelableUserMention) obj;
        if (id != other.id) return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ id >>> 32);
        return result;
    }


    @Override
    public String toString() {
        return "ParcelableUserMention{id=" + id + ", name=" + name + ", screen_name=" + screen_name + "}";
    }


    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(screen_name);
    }

    public static ParcelableUserMention[] fromUserMentionEntities(final UserMentionEntity[] entities) {
        if (entities == null) return null;
        final ParcelableUserMention[] mentions = new ParcelableUserMention[entities.length];
        for (int i = 0, j = entities.length; i < j; i++) {
            mentions[i] = new ParcelableUserMention(entities[i]);
        }
        return mentions;
    }

}
