package org.mariotaku.twidere.model;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.twidere.annotation.AccountType;
import org.mariotaku.twidere.model.account.AccountExtras;
import org.mariotaku.twidere.model.account.cred.Credentials;

/**
 * Created by mariotaku on 2016/12/3.
 */

@ParcelablePlease
public class AccountDetails implements Parcelable, Comparable<AccountDetails> {

    public boolean dummy;
    public Account account;
    public UserKey key;
    public Credentials credentials;
    public ParcelableUser user;
    @ColorInt
    public int color;
    public int position;
    public boolean activated;
    @AccountType
    public String type;
    @Credentials.Type
    public String credentials_type;
    public AccountExtras extras;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        AccountDetailsParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "AccountDetails{" +
                "account=" + account +
                ", dummy=" + dummy +
                ", key=" + key +
                ", credentials=" + credentials +
                ", user=" + user +
                ", color=" + color +
                ", position=" + position +
                ", activated=" + activated +
                ", type='" + type + '\'' +
                ", credentials_type='" + credentials_type + '\'' +
                ", extras=" + extras +
                '}';
    }

    @Override
    public int compareTo(@NonNull AccountDetails that) {
        return this.position - that.position;
    }

    @NonNull
    public static AccountDetails dummy() {
        AccountDetails dummy = new AccountDetails();
        dummy.dummy = true;
        return dummy;
    }

    public static final Creator<AccountDetails> CREATOR = new Creator<AccountDetails>() {
        public AccountDetails createFromParcel(Parcel source) {
            AccountDetails target = new AccountDetails();
            AccountDetailsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public AccountDetails[] newArray(int size) {
            return new AccountDetails[size];
        }
    };
}
