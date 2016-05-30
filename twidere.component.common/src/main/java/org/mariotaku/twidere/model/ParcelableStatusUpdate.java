/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import java.util.Arrays;

@ParcelablePlease
@JsonObject
public class ParcelableStatusUpdate implements Parcelable {

    @SuppressWarnings("NullableProblems")
    @JsonField(name = "accounts")
    @NonNull
    @ParcelableThisPlease
    public ParcelableAccount[] accounts;
    @JsonField(name = "media")
    @ParcelableThisPlease
    public ParcelableMediaUpdate[] media;
    @JsonField(name = "text")
    @ParcelableThisPlease
    public String text;
    @JsonField(name = "location")
    @ParcelableThisPlease
    public ParcelableLocation location;
    @JsonField(name = "display_coordinates")
    @ParcelableThisPlease
    public boolean display_coordinates = true;
    @JsonField(name = "in_reply_to_status")
    @ParcelableThisPlease
    public ParcelableStatus in_reply_to_status;
    @JsonField(name = "is_possibly_sensitive")
    @ParcelableThisPlease
    public boolean is_possibly_sensitive;
    @JsonField(name = "repost_status_id")
    @ParcelableThisPlease
    public String repost_status_id;
    @JsonField(name = "attachment_url")
    @ParcelableThisPlease
    public String attachment_url;

    public ParcelableStatusUpdate() {
    }

    @Override
    public String toString() {
        return "ParcelableStatusUpdate{" +
                "accounts=" + Arrays.toString(accounts) +
                ", media=" + Arrays.toString(media) +
                ", text='" + text + '\'' +
                ", location=" + location +
                ", display_coordinates=" + display_coordinates +
                ", in_reply_to_status=" + in_reply_to_status +
                ", is_possibly_sensitive=" + is_possibly_sensitive +
                ", repost_status_id='" + repost_status_id + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableStatusUpdateParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableStatusUpdate> CREATOR = new Creator<ParcelableStatusUpdate>() {
        @Override
        public ParcelableStatusUpdate createFromParcel(Parcel source) {
            ParcelableStatusUpdate target = new ParcelableStatusUpdate();
            ParcelableStatusUpdateParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableStatusUpdate[] newArray(int size) {
            return new ParcelableStatusUpdate[size];
        }
    };
}
