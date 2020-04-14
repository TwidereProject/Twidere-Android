package org.mariotaku.twidere.model.event;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;

/**
 * Created by mariotaku on 16/3/30.
 */
public class UserListMembersChangedEvent {

    @Action
    private final int action;
    @NonNull
    private final ParcelableUserList userList;
    @NonNull
    private final ParcelableUser[] users;

    public UserListMembersChangedEvent(@Action int action, @NonNull ParcelableUserList userList,
                                       @NonNull ParcelableUser[] users) {
        this.action = action;
        this.userList = userList;
        this.users = users;
    }

    @Action
    public int getAction() {
        return action;
    }

    @NonNull
    public ParcelableUserList getUserList() {
        return userList;
    }

    @NonNull
    public ParcelableUser[] getUsers() {
        return users;
    }

    @IntDef({Action.ADDED, Action.REMOVED})
    public @interface Action {
        int ADDED = 1;
        int REMOVED = 2;
    }

}
