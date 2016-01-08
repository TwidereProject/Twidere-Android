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

/**
 * Created by mariotaku on 15/11/11.
 */
@ParcelablePlease
@JsonObject
public class ScreenEvent extends BaseEvent implements Parcelable {

    @JsonField(name = "action")
    @Action
    String action;
    @JsonField(name = "present_duration")
    long presentDuration;

    public static ScreenEvent create(Context context, @Action String action, long presentDuration) {
        final ScreenEvent event = new ScreenEvent();
        event.markStart(context);
        event.setAction(action);
        event.setPresentDuration(presentDuration);
        return event;
    }

    public
    @Action
    String getAction() {
        return action;
    }

    public void setAction(@Action String action) {
        this.action = action;
    }

    public long getPresentDuration() {
        return presentDuration;
    }

    public void setPresentDuration(long presentDuration) {
        this.presentDuration = presentDuration;
    }

    @Override
    public String toString() {
        return "ScreenEvent{" +
                "action=" + action +
                ", presentDuration=" + presentDuration +
                "} " + super.toString();
    }

    public @interface Action {
        String ON = "on", OFF = "off", PRESENT = "present", UNKNOWN = "unknown";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ScreenEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ScreenEvent> CREATOR = new Creator<ScreenEvent>() {
        public ScreenEvent createFromParcel(Parcel source) {
            ScreenEvent target = new ScreenEvent();
            ScreenEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ScreenEvent[] newArray(int size) {
            return new ScreenEvent[size];
        }
    };
}
