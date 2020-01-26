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
import androidx.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 15/7/8.
 */
@ParcelablePlease
@JsonObject
public class ExtendedProfile implements Parcelable {

    @JsonField(name = "id")
    long id;
    @JsonField(name = "birthdate")
    Birthdate birthdate;

    public long getId() {
        return id;
    }

    public Birthdate getBirthdate() {
        return birthdate;
    }

    @ParcelablePlease
    @JsonObject
    public static class Birthdate implements Parcelable {

        @JsonField(name = "day")
        int day;
        @JsonField(name = "month")
        int month;
        @JsonField(name = "year")
        int year;
        @JsonField(name = "visibility")
        @Visibility
        String visibility;
        @JsonField(name = "year_visibility")
        @Visibility
        String yearVisibility;

        public int getDay() {
            return day;
        }

        public int getMonth() {
            return month;
        }

        public int getYear() {
            return year;
        }

        public
        @Visibility
        String getVisibility() {
            return visibility;
        }

        public
        @Visibility
        String getYearVisibility() {
            return yearVisibility;
        }

        @StringDef({Visibility.MUTUALFOLLOW, Visibility.PUBLIC})
        @Retention(RetentionPolicy.SOURCE)
        public @interface Visibility {
            String MUTUALFOLLOW = "mutualfollow";
            String PUBLIC = "public";

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            ExtendedProfile$BirthdateParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Birthdate> CREATOR = new Creator<Birthdate>() {
            @Override
            public Birthdate createFromParcel(Parcel source) {
                Birthdate target = new Birthdate();
                ExtendedProfile$BirthdateParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public Birthdate[] newArray(int size) {
                return new Birthdate[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ExtendedProfileParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ExtendedProfile> CREATOR = new Creator<ExtendedProfile>() {
        @Override
        public ExtendedProfile createFromParcel(Parcel source) {
            ExtendedProfile target = new ExtendedProfile();
            ExtendedProfileParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ExtendedProfile[] newArray(int size) {
            return new ExtendedProfile[size];
        }
    };
}
