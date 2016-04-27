package org.mariotaku.twidere.model;

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
public class StatusNetAccountExtra implements Parcelable, AccountExtras {

    public static final Creator<StatusNetAccountExtra> CREATOR = new Creator<StatusNetAccountExtra>() {
        @Override
        public StatusNetAccountExtra createFromParcel(Parcel source) {
            StatusNetAccountExtra target = new StatusNetAccountExtra();
            StatusNetAccountExtraParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public StatusNetAccountExtra[] newArray(int size) {
            return new StatusNetAccountExtra[size];
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
        StatusNetAccountExtraParcelablePlease.writeToParcel(this, dest, flags);
    }
}
