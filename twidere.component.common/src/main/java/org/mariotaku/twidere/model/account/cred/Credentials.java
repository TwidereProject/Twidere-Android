package org.mariotaku.twidere.model.account.cred;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 2016/12/2.
 */

@ParcelablePlease
@JsonObject
public class Credentials implements Parcelable {
    @JsonField(name = "api_url_format")
    public String api_url_format;
    @JsonField(name = "no_version_suffix")
    public boolean no_version_suffix;

    @StringDef({Type.OAUTH, Type.XAUTH, Type.BASIC, Type.EMPTY, Type.OAUTH2})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {

        String OAUTH = "oauth";
        String XAUTH = "xauth";
        String BASIC = "basic";
        String EMPTY = "empty";
        String OAUTH2 = "oauth2";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        CredentialsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<Credentials> CREATOR = new Creator<Credentials>() {
        public Credentials createFromParcel(Parcel source) {
            Credentials target = new Credentials();
            CredentialsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public Credentials[] newArray(int size) {
            return new Credentials[size];
        }
    };
}
