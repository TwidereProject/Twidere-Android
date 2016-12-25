package org.mariotaku.twidere.model.premium;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 2016/12/25.
 */

@ParcelablePlease
public class GooglePurchaseResult implements PurchaseResult, Parcelable {
    @Override
    public boolean isValid(Context context) {
        return true;
    }

    @Override
    public boolean load(Context context) {
        return true;
    }

    @Override
    public boolean save(Context context) {
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        GooglePurchaseResultParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<GooglePurchaseResult> CREATOR = new Creator<GooglePurchaseResult>() {
        public GooglePurchaseResult createFromParcel(Parcel source) {
            GooglePurchaseResult target = new GooglePurchaseResult();
            GooglePurchaseResultParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public GooglePurchaseResult[] newArray(int size) {
            return new GooglePurchaseResult[size];
        }
    };
}
