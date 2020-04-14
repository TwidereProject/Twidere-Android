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
public class CursorPagination implements Pagination, Parcelable {

    String cursor;

    CursorPagination() {
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    @Override
    public void applyTo(Paging paging) {
        paging.cursor(cursor);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        CursorPaginationParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<CursorPagination> CREATOR = new Creator<CursorPagination>() {
        public CursorPagination createFromParcel(Parcel source) {
            CursorPagination target = new CursorPagination();
            CursorPaginationParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public CursorPagination[] newArray(int size) {
            return new CursorPagination[size];
        }
    };

    @Nullable
    public static CursorPagination valueOf(String cursor) {
        if (cursor == null) return null;
        final CursorPagination pagination = new CursorPagination();
        pagination.cursor = cursor;
        return pagination;
    }

    @Nullable
    public static CursorPagination valueOf(long cursor) {
        if (cursor == 0) return null;
        final CursorPagination pagination = new CursorPagination();
        pagination.cursor = String.valueOf(cursor);
        return pagination;
    }
}
