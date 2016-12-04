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
public class OAuth2Credentials extends Credentials implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        OAuth2CredentialsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<OAuth2Credentials> CREATOR = new Creator<OAuth2Credentials>() {
        public OAuth2Credentials createFromParcel(Parcel source) {
            OAuth2Credentials target = new OAuth2Credentials();
            OAuth2CredentialsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public OAuth2Credentials[] newArray(int size) {
            return new OAuth2Credentials[size];
        }
    };
}
