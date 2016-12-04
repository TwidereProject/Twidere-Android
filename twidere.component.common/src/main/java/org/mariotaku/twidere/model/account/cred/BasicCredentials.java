package org.mariotaku.twidere.model.account.cred;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 2016/12/2.
 */

@ParcelablePlease
@JsonObject
public class BasicCredentials extends Credentials implements Parcelable {
    @JsonField(name = "username")
    public String username;
    @JsonField(name = "password")
    public String password;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        BasicCredentialsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<BasicCredentials> CREATOR = new Creator<BasicCredentials>() {
        public BasicCredentials createFromParcel(Parcel source) {
            BasicCredentials target = new BasicCredentials();
            BasicCredentialsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public BasicCredentials[] newArray(int size) {
            return new BasicCredentials[size];
        }
    };
}
