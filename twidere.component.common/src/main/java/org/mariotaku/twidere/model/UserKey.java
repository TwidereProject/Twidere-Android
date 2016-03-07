package org.mariotaku.twidere.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.twidere.api.twitter.util.TwitterDateConverter;

import java.util.List;

/**
 * Created by mariotaku on 16/3/5.
 */
@JsonObject
@ParcelablePlease
public class UserKey implements Comparable<UserKey>, Parcelable {

    public static final Creator<UserKey> CREATOR = new Creator<UserKey>() {
        public UserKey createFromParcel(Parcel source) {
            UserKey target = new UserKey();
            UserKeyParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UserKey[] newArray(int size) {
            return new UserKey[size];
        }
    };

    @JsonField(name = "id")
    @ParcelableThisPlease
    long id;
    @JsonField(name = "host")
    @ParcelableThisPlease
    String host;

    public UserKey(long id, String host) {
        this.id = id;
        this.host = host;
    }

    UserKey() {

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

    @Override
    public int compareTo(@NonNull UserKey another) {
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

        UserKey accountKey = (UserKey) o;

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
        UserKeyParcelablePlease.writeToParcel(this, dest, flags);
    }

    public boolean check(long accountId, String accountHost) {
        return this.id == accountId;
    }

    @Nullable
    public static UserKey valueOf(@Nullable String str) {
        if (str == null) return null;
        int idxOfAt = str.indexOf("@");
        try {
            if (idxOfAt != -1) {
                final String idStr = str.substring(0, idxOfAt);
                return new UserKey(Long.parseLong(idStr),
                        str.substring(idxOfAt + 1, str.length()));

            } else {
                return new UserKey(Long.parseLong(str), null);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    public static UserKey[] arrayOf(@Nullable String str) {
        if (str == null) return null;
        List<String> split = TwitterDateConverter.split(str, ",");
        UserKey[] keys = new UserKey[split.size()];
        for (int i = 0, splitLength = split.size(); i < splitLength; i++) {
            keys[i] = valueOf(split.get(i));
            if (keys[i] == null) return null;
        }
        return keys;
    }

    public static long[] getIds(UserKey[] ids) {
        long[] result = new long[ids.length];
        for (int i = 0, idsLength = ids.length; i < idsLength; i++) {
            result[i] = ids[i].getId();
        }
        return result;
    }

    public boolean maybeEquals(@Nullable UserKey another) {
        return another != null && another.getId() == id;
    }
}
