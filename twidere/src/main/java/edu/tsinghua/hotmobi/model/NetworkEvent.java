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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * Created by mariotaku on 15/8/20.
 */
@ParcelablePlease
@JsonObject
public class NetworkEvent extends BaseEvent {

    @ParcelableThisPlease
    @JsonField(name = "network_type")
    int networkType;


    public static NetworkEvent create(Context context) {
        final NetworkEvent event = new NetworkEvent();
        event.markStart(context);
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            event.setNetworkType(activeNetworkInfo.getType());
        }
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
}
