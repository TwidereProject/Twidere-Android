package org.mariotaku.twidere.model.account.cred;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 2016/12/2.
 */

@ParcelablePlease
@JsonObject
public class EmptyCredentials extends Credentials implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        EmptyCredentialsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<EmptyCredentials> CREATOR = new Creator<EmptyCredentials>() {
        public EmptyCredentials createFromParcel(Parcel source) {
            EmptyCredentials target = new EmptyCredentials();
            EmptyCredentialsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public EmptyCredentials[] newArray(int size) {
            return new EmptyCredentials[size];
        }
    };
}
