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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import java.util.Arrays;

/**
 * Created by mariotaku on 15/8/8.
 */
@ParcelablePlease
@JsonObject
public class RefreshEvent extends BaseEvent {
    @ParcelableThisPlease
    @JsonField(name = "ids")
    long[] ids;

    @ParcelableThisPlease
    @JsonField(name = "timeline_type", typeConverter = TimelineType.TimelineTypeConverter.class)
    TimelineType timelineType;

    public static RefreshEvent create(final Context context, long[] ids, TimelineType timelineType) {
        final RefreshEvent event = new RefreshEvent();
        event.markStart(context);
        event.setIds(ids);
        event.setTimelineType(timelineType);
        return event;
    }

    public void setIds(long[] ids) {
        this.ids = ids;
    }

    public void setTimelineType(TimelineType timelineType) {
        this.timelineType = timelineType;
    }

    @Override
    public String toString() {
        return "RefreshEvent{" +
                "ids=" + Arrays.toString(ids) +
                ", timelineType=" + timelineType +
                "} " + super.toString();
    }
}
