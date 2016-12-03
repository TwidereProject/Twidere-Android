package org.mariotaku.twidere.model.account;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * Created by mariotaku on 16/2/26.
 */
@ParcelablePlease
@JsonObject
public class StatusNetAccountExtras implements Parcelable, AccountExtras {

    public static final Creator<StatusNetAccountExtras> CREATOR = new Creator<StatusNetAccountExtras>() {
        @Override
        public StatusNetAccountExtras createFromParcel(Parcel source) {
            StatusNetAccountExtras target = new StatusNetAccountExtras();
            StatusNetAccountExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public StatusNetAccountExtras[] newArray(int size) {
            return new StatusNetAccountExtras[size];
        }
    };

    @ParcelableThisPlease
    @JsonField(name = "text_limit")
    int textLimit;

    public int getTextLimit() {
        return textLimit;
    }

    public void setTextLimit(int textLimit) {
        this.textLimit = textLimit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StatusNetAccountExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }
}
