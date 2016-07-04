package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

@ParcelablePlease
@JsonObject
public class StatusShortenResult implements Parcelable {

    @JsonField(name = "shortened")
    @ParcelableThisPlease
    public String shortened;

    @JsonField(name = "extra")
    @ParcelableThisPlease
    public String extra;

    @JsonField(name = "error_code")
    @ParcelableThisPlease
    public int error_code;

    @JsonField(name = "error_message")
    @ParcelableThisPlease
    public String error_message;

    @ParcelableThisPlease
    @JsonField(name = "shared_owners")
    public UserKey[] shared_owners;

    public StatusShortenResult() {
    }

    public StatusShortenResult(final int errorCode, final String errorMessage) {
        if (errorCode == 0) throw new IllegalArgumentException("Error code must not be 0");
        shortened = null;
        error_code = errorCode;
        error_message = errorMessage;
    }

    public StatusShortenResult(final String shortened) {
        if (shortened == null)
            throw new IllegalArgumentException("Shortened text must not be null");
        this.shortened = shortened;
        error_code = 0;
        error_message = null;
    }

    @Override
    public String toString() {
        return "StatusShortenResult{" +
                "shortened='" + shortened + '\'' +
                ", extra='" + extra + '\'' +
                ", error_code=" + error_code +
                ", error_message='" + error_message + '\'' +
                '}';
    }

    public static StatusShortenResult error(final int errorCode, final String errorMessage) {
        return new StatusShortenResult(errorCode, errorMessage);
    }

    public static StatusShortenResult shortened(final String shortened) {
        return new StatusShortenResult(shortened);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StatusShortenResultParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<StatusShortenResult> CREATOR = new Creator<StatusShortenResult>() {
        @Override
        public StatusShortenResult createFromParcel(Parcel source) {
            StatusShortenResult target = new StatusShortenResult();
            StatusShortenResultParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public StatusShortenResult[] newArray(int size) {
            return new StatusShortenResult[size];
        }
    };
}
