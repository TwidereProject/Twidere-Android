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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import java.util.TimeZone;

import edu.tsinghua.hotmobi.HotMobiLogger;

/**
 * Created by mariotaku on 15/8/8.
 */
@ParcelablePlease
@JsonObject
public class BaseEvent implements Parcelable {

    public static final Creator<BaseEvent> CREATOR = new Creator<BaseEvent>() {
        @Override
        public BaseEvent createFromParcel(Parcel in) {
            return new BaseEvent(in);
        }

        @Override
        public BaseEvent[] newArray(int size) {
            return new BaseEvent[size];
        }
    };

    @ParcelableThisPlease
    @JsonField(name = "start_time")
    long startTime;
    @ParcelableThisPlease
    @JsonField(name = "end_time")
    long endTime;
    @ParcelableThisPlease
    @JsonField(name = "time_offset")
    long timeOffset;
    @ParcelableThisPlease
    @JsonField(name = "location")
    LatLng location;

    public BaseEvent() {
    }

    protected BaseEvent(Parcel in) {
        BaseEventParcelablePlease.readFromParcel(this, in);
    }


    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setTimeOffset(long timeOffset) {
        this.timeOffset = timeOffset;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void markStart(Context context) {
        setStartTime(System.currentTimeMillis());
        setTimeOffset(TimeZone.getDefault().getOffset(startTime));
        setLocation(HotMobiLogger.getCachedLatLng(context));
    }

    public void markEnd() {
        setEndTime(System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "BaseEvent{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", timeOffset=" + timeOffset +
                ", location=" + location +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        BaseEventParcelablePlease.writeToParcel(this, dest, flags);
    }
}
