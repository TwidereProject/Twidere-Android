package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 16/2/26.
 */
@ParcelablePlease
public class TwitterAccountExtra implements Parcelable, AccountExtras {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TwitterAccountExtraParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TwitterAccountExtra> CREATOR = new Creator<TwitterAccountExtra>() {
        public TwitterAccountExtra createFromParcel(Parcel source) {
            TwitterAccountExtra target = new TwitterAccountExtra();
            TwitterAccountExtraParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TwitterAccountExtra[] newArray(int size) {
            return new TwitterAccountExtra[size];
        }
    };
}
