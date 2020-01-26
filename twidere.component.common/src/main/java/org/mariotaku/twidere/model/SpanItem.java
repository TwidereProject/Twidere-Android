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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.IntDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 16/3/21.
 */
@JsonObject
@ParcelablePlease
public class SpanItem implements Parcelable {
    public static final Creator<SpanItem> CREATOR = new Creator<SpanItem>() {
        @Override
        public SpanItem createFromParcel(Parcel in) {
            final SpanItem obj = new SpanItem();
            SpanItemParcelablePlease.readFromParcel(obj, in);
            return obj;
        }

        @Override
        public SpanItem[] newArray(int size) {
            return new SpanItem[size];
        }
    };

    @JsonField(name = "start")
    @ParcelableThisPlease
    public int start;
    @JsonField(name = "end")
    @ParcelableThisPlease
    public int end;
    @JsonField(name = "link")
    @ParcelableThisPlease
    public String link;

    @ParcelableThisPlease
    @JsonField(name = "type")
    @SpanType
    public int type = SpanType.LINK;

    @ParcelableNoThanks
    public int orig_start = -1;
    @ParcelableNoThanks
    public int orig_end = -1;

    @Override
    public String toString() {
        return "SpanItem{" +
                "start=" + start +
                ", end=" + end +
                ", link='" + link + '\'' +
                ", type=" + type +
                ", orig_start=" + orig_start +
                ", orig_end=" + orig_end +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SpanItemParcelablePlease.writeToParcel(this, dest, flags);
    }

    @IntDef({SpanType.HIDE, SpanType.LINK, SpanType.ACCT_MENTION, SpanType.HASHTAG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SpanType {
        int HIDE = -1;
        int LINK = 0;
        int ACCT_MENTION = 1;
        int HASHTAG = 2;
    }
}
