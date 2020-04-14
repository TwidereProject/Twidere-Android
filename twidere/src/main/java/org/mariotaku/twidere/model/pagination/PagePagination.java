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
public class PagePagination implements Pagination, Parcelable {

    int page;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public void applyTo(Paging paging) {
        paging.page(page);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        PagePaginationParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<PagePagination> CREATOR = new Creator<PagePagination>() {
        public PagePagination createFromParcel(Parcel source) {
            PagePagination target = new PagePagination();
            PagePaginationParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public PagePagination[] newArray(int size) {
            return new PagePagination[size];
        }
    };

    @Nullable
    public static PagePagination valueOf(int page) {
        if (page <= 0) return null;
        final PagePagination pagination = new PagePagination();
        pagination.page = page;
        return pagination;
    }
}
