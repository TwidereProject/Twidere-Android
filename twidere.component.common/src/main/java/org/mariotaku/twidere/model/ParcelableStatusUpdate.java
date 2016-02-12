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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.util.Arrays;

@ParcelablePlease
@JsonObject
public class ParcelableStatusUpdate implements Parcelable {

    @JsonField(name = "accounts")
    public ParcelableAccount[] accounts;
    @JsonField(name = "media")
    public ParcelableMediaUpdate[] media;
    @JsonField(name = "text")
    public String text;
    @JsonField(name = "location")
    public ParcelableLocation location;
    @JsonField(name = "in_reply_to_status_id")
    public long in_reply_to_status_id;
    @JsonField(name = "boolean")
    public boolean is_possibly_sensitive;

    public ParcelableStatusUpdate() {
    }

    /**
     * It has too much arguments to call, use
     * <b>ParcelableStatusUpdate.Builder</b> instead.
     */
    public ParcelableStatusUpdate(final ParcelableAccount[] accounts, final String text, final ParcelableLocation location,
                                  final ParcelableMediaUpdate[] media, final long in_reply_to_status_id, final boolean is_possibly_sensitive) {
        this.accounts = accounts;
        this.text = text;
        this.location = location;
        this.media = media;
        this.in_reply_to_status_id = in_reply_to_status_id;
        this.is_possibly_sensitive = is_possibly_sensitive;
    }

    @Override
    public String toString() {
        return "ParcelableStatusUpdate{" +
                "accounts=" + Arrays.toString(accounts) +
                ", media=" + Arrays.toString(media) +
                ", text='" + text + '\'' +
                ", location=" + location +
                ", in_reply_to_status_id=" + in_reply_to_status_id +
                ", is_possibly_sensitive=" + is_possibly_sensitive +
                '}';
    }

    public static final class Builder {

        private ParcelableAccount[] accounts;
        private String text;
        private ParcelableLocation location;
        private ParcelableMediaUpdate[] media;
        private long in_reply_to_status_id;
        private boolean is_possibly_sensitive;

        public Builder() {

        }

        public Builder(final ParcelableStatusUpdate base) {
            accounts(base.accounts);
            text(base.text);
            media(base.media);
            location(base.location);
            inReplyToStatusId(base.in_reply_to_status_id);
            isPossiblySensitive(base.is_possibly_sensitive);
        }

        public Builder accounts(final ParcelableAccount[] accounts) {
            this.accounts = accounts;
            return this;
        }

        public ParcelableStatusUpdate build() {
            return new ParcelableStatusUpdate(accounts, text, location, media, in_reply_to_status_id,
                    is_possibly_sensitive);
        }

        public Builder inReplyToStatusId(final long in_reply_to_status_id) {
            this.in_reply_to_status_id = in_reply_to_status_id;
            return this;
        }

        public Builder isPossiblySensitive(final boolean is_possibly_sensitive) {
            this.is_possibly_sensitive = is_possibly_sensitive;
            return this;
        }

        public Builder location(final ParcelableLocation location) {
            this.location = location;
            return this;
        }

        public Builder media(final ParcelableMediaUpdate... media) {
            this.media = media;
            return this;
        }

        public Builder text(final String text) {
            this.text = text;
            return this;
        }
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
        public ParcelableStatusUpdate createFromParcel(Parcel source) {
            ParcelableStatusUpdate target = new ParcelableStatusUpdate();
            ParcelableStatusUpdateParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableStatusUpdate[] newArray(int size) {
            return new ParcelableStatusUpdate[size];
        }
    };
}
