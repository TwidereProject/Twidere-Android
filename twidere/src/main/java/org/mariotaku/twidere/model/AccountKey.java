package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

/**
 * Created by mariotaku on 16/3/5.
 */
@JsonObject
@ParcelablePlease
public class AccountKey implements Comparable<AccountKey>, Parcelable {

    @JsonField(name = "id")
    @ParcelableThisPlease
    long id;
    @JsonField(name = "host")
    @ParcelableThisPlease
    String host;

    public AccountKey(long id, String host) {
        this.id = id;
        this.host = host;
    }

    public AccountKey(ParcelableAccount account) {
        this.id = account.account_id;
        this.host = account.account_host;
    }

    AccountKey() {

    }

    public long getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        if (host != null) return id + "@" + host;
        return String.valueOf(id);
    }

    @Nullable
    public static AccountKey valueOf(@Nullable String str) {
        if (str == null) return null;
        int idxOfAt = str.indexOf("@");
        try {
            if (idxOfAt != -1) {
                final String idStr = str.substring(0, idxOfAt);
                return new AccountKey(Long.parseLong(idStr),
                        str.substring(idxOfAt + 1, str.length()));

            } else {
                return new AccountKey(Long.parseLong(str), null);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static long[] getIds(AccountKey[] ids) {
        long[] result = new long[ids.length];
        for (int i = 0, idsLength = ids.length; i < idsLength; i++) {
            result[i] = ids[i].getId();
        }
        return result;
    }

    @Override
    public int compareTo(@NonNull AccountKey another) {
        if (this.id == another.id) {
            if (this.host != null && another.host != null) {
                return this.host.compareTo(another.host);
            } else if (this.host != null) {
                return 1;
            } else if (another.host != null) {
                return -1;
            }
            return 0;
        }
        return (int) (this.id - another.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountKey accountKey = (AccountKey) o;

        return id == accountKey.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        AccountKeyParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<AccountKey> CREATOR = new Creator<AccountKey>() {
        public AccountKey createFromParcel(Parcel source) {
            AccountKey target = new AccountKey();
            AccountKeyParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public AccountKey[] newArray(int size) {
            return new AccountKey[size];
        }
    };
}
