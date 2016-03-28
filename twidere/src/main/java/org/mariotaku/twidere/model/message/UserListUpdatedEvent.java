package org.mariotaku.twidere.model.message;

import android.support.annotation.NonNull;

import org.mariotaku.twidere.model.ParcelableUserList;

/**
 * Created by mariotaku on 16/3/28.
 */
public class UserListUpdatedEvent {
    private final ParcelableUserList userList;

    public UserListUpdatedEvent(@NonNull ParcelableUserList userList) {
        this.userList = userList;
    }

    public ParcelableUserList getUserList() {
        return userList;
    }
}
