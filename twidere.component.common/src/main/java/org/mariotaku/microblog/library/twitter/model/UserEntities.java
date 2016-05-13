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

package org.mariotaku.microblog.library.twitter.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 15/3/31.
 */
@ParcelablePlease
@JsonObject
public class UserEntities implements Parcelable {

    @JsonField(name = "url")
    Entities url;

    @JsonField(name = "description")
    Entities description;

    public UrlEntity[] getDescriptionEntities() {
        if (description == null) return null;
        return description.getUrls();
    }

    public UrlEntity[] getUrlEntities() {
        if (url == null) return null;
        return url.getUrls();
    }

    @Override
    public String toString() {
        return "UserEntities{" +
                "url=" + url +
                ", description=" + description +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserEntitiesParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UserEntities> CREATOR = new Creator<UserEntities>() {
        @Override
        public UserEntities createFromParcel(Parcel source) {
            UserEntities target = new UserEntities();
            UserEntitiesParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public UserEntities[] newArray(int size) {
            return new UserEntities[size];
        }
    };
}
