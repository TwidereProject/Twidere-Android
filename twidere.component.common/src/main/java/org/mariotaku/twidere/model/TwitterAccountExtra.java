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
public class TwitterAccountExtra implements Parcelable, AccountExtras {

    public static final Creator<TwitterAccountExtra> CREATOR = new Creator<TwitterAccountExtra>() {
        @Override
        public TwitterAccountExtra createFromParcel(Parcel source) {
            TwitterAccountExtra target = new TwitterAccountExtra();
            TwitterAccountExtraParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public TwitterAccountExtra[] newArray(int size) {
            return new TwitterAccountExtra[size];
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
        TwitterAccountExtraParcelablePlease.writeToParcel(this, dest, flags);
    }
}
