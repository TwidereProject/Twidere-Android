/*
 *         Twidere - Twitter client for Android
 *
 * Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.model.tab.extra;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.annotation.TimelineStyle;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_TIMELINE_STYLE;

@JsonObject
@ParcelablePlease
public class TimelineTabExtras extends TabExtras implements Parcelable {
    @JsonField(name = "timeline_style")
    @TimelineStyle
    int timelineStyle;

    @Override
    public void copyToBundle(Bundle bundle) {
        super.copyToBundle(bundle);
        bundle.putInt(EXTRA_TIMELINE_STYLE, timelineStyle);
    }

    @TimelineStyle
    public int getTimelineStyle() {
        return timelineStyle;
    }

    public void setTimelineStyle(@TimelineStyle int timelineStyle) {
        this.timelineStyle = timelineStyle;
    }

    @Override
    public String toString() {
        return "TimelineTabExtras{" +
                "timelineStyle=" + timelineStyle +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TimelineTabExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TimelineTabExtras> CREATOR = new Creator<TimelineTabExtras>() {
        public TimelineTabExtras createFromParcel(Parcel source) {
            TimelineTabExtras target = new TimelineTabExtras();
            TimelineTabExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TimelineTabExtras[] newArray(int size) {
            return new TimelineTabExtras[size];
        }
    };
}
