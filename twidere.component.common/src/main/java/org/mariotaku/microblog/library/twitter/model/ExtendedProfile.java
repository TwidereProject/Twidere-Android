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
import android.support.annotation.StringDef;

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
