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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * Created by mariotaku on 15/8/20.
 */
@ParcelablePlease
@JsonObject
public class NetworkEvent extends BaseEvent implements Parcelable {

    @JsonField(name = "network_type")
    @ParcelableThisPlease
    int networkType;

    public static NetworkEvent create(@NonNull Context context) {
        final NetworkEvent event = new NetworkEvent();
        event.markStart(context);
        event.setNetworkType(getActivateNetworkType(context));
        return event;
    }

    public void setNetworkType(int networkType) {
        this.networkType = networkType;
    }

    @Override
    public String toString() {
        return "NetworkEvent{" +
                "networkType=" + networkType +
                "} " + super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        NetworkEventParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static int getActivateNetworkType(Context context) {
        try {
            final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return activeNetworkInfo.getType();
            }
            return -1;
        } catch (SecurityException e) {
            return -1;
        }

    }

    public static final Creator<NetworkEvent> CREATOR = new Creator<NetworkEvent>() {
        @Override
        public NetworkEvent createFromParcel(Parcel source) {
            NetworkEvent target = new NetworkEvent();
            NetworkEventParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public NetworkEvent[] newArray(int size) {
            return new NetworkEvent[size];
        }
    };

    @NonNull
    @Override
    public String getLogFileName() {
        return "network";
    }
}
