package org.mariotaku.twidere.model.tab.argument;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

/**
 * Created by mariotaku on 16/3/6.
 */
@ParcelablePlease
@JsonObject
public class UserListArguments extends TabArguments implements Parcelable {
    @JsonField(name = "list_id")
    String listId;

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    @Override
    public void copyToBundle(@NonNull Bundle bundle) {
        super.copyToBundle(bundle);
        bundle.putString(EXTRA_LIST_ID, listId);
    }

    @Override
    public String toString() {
        return "UserListArguments{" +
                "listId=" + listId +
                "} " + super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserListArgumentsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UserListArguments> CREATOR = new Creator<UserListArguments>() {
        public UserListArguments createFromParcel(Parcel source) {
            UserListArguments target = new UserListArguments();
            UserListArgumentsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UserListArguments[] newArray(int size) {
            return new UserListArguments[size];
        }
    };
}
