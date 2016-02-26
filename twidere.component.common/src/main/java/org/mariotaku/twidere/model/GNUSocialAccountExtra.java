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
public class GNUSocialAccountExtra implements Parcelable, AccountExtras {

    @ParcelableThisPlease
    @JsonField(name = "character_limit")
    int characterLimit;

    public int getCharacterLimit() {
        return characterLimit;
    }

    public void setCharacterLimit(int characterLimit) {
        this.characterLimit = characterLimit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        GNUSocialAccountExtraParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<GNUSocialAccountExtra> CREATOR = new Creator<GNUSocialAccountExtra>() {
        public GNUSocialAccountExtra createFromParcel(Parcel source) {
            GNUSocialAccountExtra target = new GNUSocialAccountExtra();
            GNUSocialAccountExtraParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public GNUSocialAccountExtra[] newArray(int size) {
            return new GNUSocialAccountExtra[size];
        }
    };
}
