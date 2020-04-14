package org.mariotaku.twidere.model.event;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import org.mariotaku.twidere.model.ParcelableUserList;

/**
 * Created by mariotaku on 16/3/28.
 */
public class UserListSubscriptionEvent {
    @Action
    private final int action;
    @NonNull
    private final ParcelableUserList userList;

    public UserListSubscriptionEvent(@Action int action, @NonNull ParcelableUserList userList) {
        this.action = action;
        this.userList = userList;
    }

    @Action
    public int getAction() {
        return action;
    }

    @NonNull
    public ParcelableUserList getUserList() {
        return userList;
    }

    @IntDef({Action.SUBSCRIBE, Action.UNSUBSCRIBE})
    public @interface Action {
        int SUBSCRIBE = 1;
        int UNSUBSCRIBE = 2;
    }
}
