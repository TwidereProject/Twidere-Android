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

package org.mariotaku.twidere.api.twitter.model.impl;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.mariotaku.twidere.api.twitter.model.ExtendedProfile;

/**
 * Created by mariotaku on 15/7/8.
 */
@JsonObject
public class ExtendedProfileImpl implements ExtendedProfile {

    @JsonField(name = "id")
    long id;
    @JsonField(name = "birthdate")
    BirthdateImpl birthdate;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Birthdate getBirthdate() {
        return birthdate;
    }

    @JsonObject
    public static class BirthdateImpl implements Birthdate {

        @JsonField(name = "day")
        int day;
        @JsonField(name = "month")
        int month;
        @JsonField(name = "year")
        int year;
        @JsonField(name = "visibility")
        Visibility visibility;
        @JsonField(name = "year_visibility")
        Visibility yearVisibility;

        @Override
        public int getDay() {
            return day;
        }

        @Override
        public int getMonth() {
            return month;
        }

        @Override
        public int getYear() {
            return year;
        }

        @Override
        public Visibility getVisibility() {
            return visibility;
        }

        @Override
        public Visibility getYearVisibility() {
            return yearVisibility;
        }
    }
}
