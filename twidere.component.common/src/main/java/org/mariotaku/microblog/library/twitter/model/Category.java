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

@ParcelablePlease
@JsonObject
public class Category implements Parcelable {
    @JsonField(name = "name")
    String name;
    @JsonField(name = "size")
    long size;
    @JsonField(name = "slug")
    String slug;

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getSlug() {
        return slug;
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", slug='" + slug + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        CategoryParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel source) {
            Category target = new Category();
            CategoryParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}
