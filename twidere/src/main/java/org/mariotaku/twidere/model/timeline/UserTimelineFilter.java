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

package org.mariotaku.twidere.model.timeline;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.R;

/**
 * Created by mariotaku on 2017/3/31.
 */
@ParcelablePlease
public class UserTimelineFilter implements TimelineFilter, Parcelable {
    @ParcelableThisPlease
    boolean includeRetweets = true;
    @ParcelableThisPlease
    boolean includeReplies = true;

    public boolean isIncludeRetweets() {
        return includeRetweets;
    }

    public void setIncludeRetweets(final boolean includeRetweets) {
        this.includeRetweets = includeRetweets;
    }

    public boolean isIncludeReplies() {
        return includeReplies;
    }

    public void setIncludeReplies(final boolean includeReplies) {
        this.includeReplies = includeReplies;
    }

    @Override
    public CharSequence getSummary(final Context context) {
        if (includeRetweets && includeReplies) {
            return context.getString(R.string.label_statuses_retweets_replies);
        } else if (includeReplies) {
            return context.getString(R.string.label_statuses_replies);
        } else if (includeRetweets) {
            return context.getString(R.string.label_statuses_retweets);
        }
        return context.getString(R.string.label_statuses);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserTimelineFilterParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UserTimelineFilter> CREATOR = new Creator<UserTimelineFilter>() {
        public UserTimelineFilter createFromParcel(Parcel source) {
            UserTimelineFilter target = new UserTimelineFilter();
            UserTimelineFilterParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UserTimelineFilter[] newArray(int size) {
            return new UserTimelineFilter[size];
        }
    };
}
