package org.mariotaku.twidere.model.premium;

import android.os.Parcel;
import android.os.Parcelable;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 2017/1/17.
 */

@ParcelablePlease
public class PurchaseResult implements Parcelable {
    String feature;
    double price;
    String currency;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        PurchaseResultParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<PurchaseResult> CREATOR = new Creator<PurchaseResult>() {
        public PurchaseResult createFromParcel(Parcel source) {
            PurchaseResult target = new PurchaseResult();
            PurchaseResultParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public PurchaseResult[] newArray(int size) {
            return new PurchaseResult[size];
        }
    };
}
