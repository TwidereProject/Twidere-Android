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
