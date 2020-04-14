/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model.pagination;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.microblog.library.twitter.model.Paging;

/**
 * Created by mariotaku on 2017/4/21.
 */

@ParcelablePlease
public class SinceMaxPagination implements Pagination, Parcelable {

    String sinceId;
    String maxId;
    long sinceSortId;
    long maxSortId;

    public String getSinceId() {
        return sinceId;
    }

    public void setSinceId(String sinceId) {
        this.sinceId = sinceId;
    }

    public String getMaxId() {
        return maxId;
    }

    public void setMaxId(String maxId) {
        this.maxId = maxId;
    }

    public long getSinceSortId() {
        return sinceSortId;
    }

    public void setSinceSortId(long sinceSortId) {
        this.sinceSortId = sinceSortId;
    }

    public long getMaxSortId() {
        return maxSortId;
    }

    public void setMaxSortId(long maxSortId) {
        this.maxSortId = maxSortId;
    }

    @Override
    public void applyTo(Paging paging) {
        if (sinceId != null) {
            paging.sinceId(sinceId);
        }
        if (maxId != null) {
            paging.maxId(maxId);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SinceMaxPaginationParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Nullable
    public static SinceMaxPagination sinceId(String sinceId, long sinceSortId) {
        if (sinceId == null) return null;
        SinceMaxPagination pagination = new SinceMaxPagination();
        pagination.setSinceId(sinceId);
        pagination.setSinceSortId(sinceSortId);
        return pagination;
    }

    @Nullable
    public static SinceMaxPagination maxId(String maxId, long maxSortId) {
        if (maxId == null) return null;
        SinceMaxPagination pagination = new SinceMaxPagination();
        pagination.setMaxId(maxId);
        pagination.setMaxSortId(maxSortId);
        return pagination;
    }

    public static final Creator<SinceMaxPagination> CREATOR = new Creator<SinceMaxPagination>() {
        public SinceMaxPagination createFromParcel(Parcel source) {
            SinceMaxPagination target = new SinceMaxPagination();
            SinceMaxPaginationParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public SinceMaxPagination[] newArray(int size) {
            return new SinceMaxPagination[size];
        }
    };
}
