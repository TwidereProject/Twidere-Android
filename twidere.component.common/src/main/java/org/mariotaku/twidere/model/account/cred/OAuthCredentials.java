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
public class OAuthCredentials extends Credentials implements Parcelable {
    @JsonField(name = "consumer_key")
    public String consumer_key;
    @JsonField(name = "consumer_secret")
    public String consumer_secret;

    @JsonField(name = "access_token")
    public String access_token;
    @JsonField(name = "access_token_secret")
    public String access_token_secret;

    @JsonField(name = "same_oauth_signing_url")
    public boolean same_oauth_signing_url;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        OAuthCredentialsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<OAuthCredentials> CREATOR = new Creator<OAuthCredentials>() {
        public OAuthCredentials createFromParcel(Parcel source) {
            OAuthCredentials target = new OAuthCredentials();
            OAuthCredentialsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public OAuthCredentials[] newArray(int size) {
            return new OAuthCredentials[size];
        }
    };
}
