package org.mariotaku.twidere.model.tab.extra;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_WOEID;

/**
 * Created by mariotaku on 2017/2/2.
 */

@JsonObject
@ParcelablePlease
public class TrendsTabExtras extends TabExtras implements Parcelable {
    @JsonField(name = "woeid")
    int woeId;
    @JsonField(name = "place_name")
    String placeName;

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(final String placeName) {
        this.placeName = placeName;
    }

    public int getWoeId() {
        return woeId;
    }

    public void setWoeId(final int woeId) {
        this.woeId = woeId;
    }

    @Override
    public void copyToBundle(final Bundle bundle) {
        super.copyToBundle(bundle);
        bundle.putInt(EXTRA_WOEID, woeId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TrendsTabExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TrendsTabExtras> CREATOR = new Creator<TrendsTabExtras>() {
        public TrendsTabExtras createFromParcel(Parcel source) {
            TrendsTabExtras target = new TrendsTabExtras();
            TrendsTabExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TrendsTabExtras[] newArray(int size) {
            return new TrendsTabExtras[size];
        }
    };

}
