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

package edu.tsinghua.hotmobi.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import java.util.Arrays;

/**
 * Created by mariotaku on 15/8/8.
 */
@ParcelablePlease(allFields = false)
@JsonObject
public class RefreshEvent extends BaseEvent implements Parcelable {
    @JsonField(name = "ids")
    @ParcelableThisPlease
    String[] ids;

    @JsonField(name = "timeline_type")
    @ParcelableThisPlease
    @TimelineType
    String timelineType;

    public static RefreshEvent create(@NonNull final Context context, String[] ids, @TimelineType String timelineType) {
        final RefreshEvent event = new RefreshEvent();
        event.markStart(context);
        event.setIds(ids);
        event.setTimelineType(timelineType);
        return event;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
    }

    public void setTimelineType(@TimelineType String timelineType) {
        this.timelineType = timelineType;
    }

    @Override
    public String toString() {
        return "RefreshEvent{" +
                "ids=" + Arrays.toString(ids) +
                ", timelineType=" + timelineType +
                "} " + super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        RefreshEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<RefreshEvent> CREATOR = new Creator<RefreshEvent>() {
        public RefreshEvent createFromParcel(Parcel source) {
            RefreshEvent target = new RefreshEvent();
            RefreshEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public RefreshEvent[] newArray(int size) {
            return new RefreshEvent[size];
        }
    };

    @NonNull
    @Override
    public String getLogFileName() {
        return "refresh";
    }
}
