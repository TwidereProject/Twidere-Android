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

import edu.tsinghua.hotmobi.util.TwidereDataUtils;

/**
 * Created by mariotaku on 15/8/20.
 */
@JsonObject
@ParcelablePlease
public class LinkEvent extends BaseEvent implements Parcelable {

    @JsonField(name = "link")
    @ParcelableThisPlease
    String link;
    @JsonField(name = "type")
    @ParcelableThisPlease
    String type;

    public LinkEvent() {

    }

    public static LinkEvent create(@NonNull Context context, String link, int typeInt) {
        final LinkEvent event = new LinkEvent();
        event.markStart(context);
        event.setLink(link);
        event.setType(TwidereDataUtils.getLinkType(typeInt));
        return event;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "LinkEvent{" +
                "link='" + link + '\'' +
                ", type='" + type + '\'' +
                "} " + super.toString();
    }

    @NonNull
    @Override
    public String getLogFileName() {
        return "link";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        LinkEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<LinkEvent> CREATOR = new Creator<LinkEvent>() {
        public LinkEvent createFromParcel(Parcel source) {
            LinkEvent target = new LinkEvent();
            LinkEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public LinkEvent[] newArray(int size) {
            return new LinkEvent[size];
        }
    };
}
