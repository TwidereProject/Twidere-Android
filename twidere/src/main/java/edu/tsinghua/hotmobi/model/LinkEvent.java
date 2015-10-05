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

import edu.tsinghua.hotmobi.TypeMappingUtil;

/**
 * Created by mariotaku on 15/8/20.
 */
@JsonObject
public class LinkEvent extends BaseEvent implements Parcelable {

    public static final Creator<LinkEvent> CREATOR = new Creator<LinkEvent>() {
        @Override
        public LinkEvent createFromParcel(Parcel in) {
            return new LinkEvent(in);
        }

        @Override
        public LinkEvent[] newArray(int size) {
            return new LinkEvent[size];
        }
    };
    @JsonField(name = "link")
    String link;
    @JsonField(name = "type")
    String type;

    public LinkEvent() {

    }

    protected LinkEvent(Parcel in) {
        super(in);
        link = in.readString();
        type = in.readString();
    }

    public static LinkEvent create(Context context, String link, int typeInt) {
        final LinkEvent event = new LinkEvent();
        event.markStart(context);
        event.setLink(link);
        event.setType(TypeMappingUtil.getLinkType(typeInt));
        return event;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(link);
        dest.writeString(type);
    }

    @Override
    public int describeContents() {
        return 0;
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
}
