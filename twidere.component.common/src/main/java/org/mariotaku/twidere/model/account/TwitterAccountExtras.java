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
public class TwitterAccountExtras implements Parcelable, AccountExtras {

    public static final Creator<TwitterAccountExtras> CREATOR = new Creator<TwitterAccountExtras>() {
        @Override
        public TwitterAccountExtras createFromParcel(Parcel source) {
            TwitterAccountExtras target = new TwitterAccountExtras();
            TwitterAccountExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public TwitterAccountExtras[] newArray(int size) {
            return new TwitterAccountExtras[size];
        }
    };

    @JsonField(name = "official_credentials")
    @ParcelableThisPlease
    boolean officialCredentials;

    public boolean isOfficialCredentials() {
        return officialCredentials;
    }

    public void setIsOfficialCredentials(boolean officialCredentials) {
        this.officialCredentials = officialCredentials;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TwitterAccountExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }
}
