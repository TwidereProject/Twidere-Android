package org.mariotaku.twidere.model.draft;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.model.ParcelableStatus;

/**
 * Created by mariotaku on 2017/2/7.
 */

@ParcelablePlease
@JsonObject
public class StatusObjectExtras implements ActionExtras, Parcelable {
    @JsonField(name = "status")
    public ParcelableStatus status;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StatusObjectExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<StatusObjectExtras> CREATOR = new Creator<StatusObjectExtras>() {
        public StatusObjectExtras createFromParcel(Parcel source) {
            StatusObjectExtras target = new StatusObjectExtras();
            StatusObjectExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public StatusObjectExtras[] newArray(int size) {
            return new StatusObjectExtras[size];
        }
    };
}
