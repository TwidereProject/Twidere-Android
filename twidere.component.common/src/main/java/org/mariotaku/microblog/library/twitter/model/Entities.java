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

import java.util.Arrays;

/**
 * Created by mariotaku on 15/3/31.
 */
@ParcelablePlease
@JsonObject
public class Entities implements Parcelable {
    @JsonField(name = "hashtags")
    HashtagEntity[] hashtags;

    @JsonField(name = "user_mentions")
    UserMentionEntity[] userMentions;

    @JsonField(name = "urls")
    UrlEntity[] urls;

    @JsonField(name = "media")
    MediaEntity[] media;

    public HashtagEntity[] getHashtags() {
        return hashtags;
    }

    public UserMentionEntity[] getUserMentions() {
        return userMentions;
    }

    public UrlEntity[] getUrls() {
        return urls;
    }

    public MediaEntity[] getMedia() {
        return media;
    }

    @Override
    public String toString() {
        return "Entities{" +
                "hashtags=" + Arrays.toString(hashtags) +
                ", userMentions=" + Arrays.toString(userMentions) +
                ", urls=" + Arrays.toString(urls) +
                ", media=" + Arrays.toString(media) +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        EntitiesParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Entities> CREATOR = new Creator<Entities>() {
        @Override
        public Entities createFromParcel(Parcel source) {
            Entities target = new Entities();
            EntitiesParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Entities[] newArray(int size) {
            return new Entities[size];
        }
    };
}
