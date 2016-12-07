package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 2016/12/7.
 */

@ParcelablePlease
public class SyncAuthInfo implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SyncAuthInfoParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<SyncAuthInfo> CREATOR = new Creator<SyncAuthInfo>() {
        public SyncAuthInfo createFromParcel(Parcel source) {
            SyncAuthInfo target = new SyncAuthInfo();
            SyncAuthInfoParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public SyncAuthInfo[] newArray(int size) {
            return new SyncAuthInfo[size];
        }
    };
}
