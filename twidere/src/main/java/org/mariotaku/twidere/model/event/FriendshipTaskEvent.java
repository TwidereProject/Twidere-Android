package org.mariotaku.twidere.model.event;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.UserKey;

public class FriendshipTaskEvent {

    @Action
    private int action;
    private boolean finished;
    private boolean succeeded;
    @NonNull
    private UserKey accountKey;
    @NonNull
    private UserKey userKey;
    private ParcelableUser user;

    public FriendshipTaskEvent(@Action int action, @NonNull UserKey accountKey, @NonNull UserKey userKey) {
        this.action = action;
        this.accountKey = accountKey;
        this.userKey = userKey;
    }

    @Action
    public int getAction() {
        return action;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    @NonNull
    public UserKey getAccountKey() {
        return accountKey;
    }

    @NonNull
    public UserKey getUserKey() {
        return userKey;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public ParcelableUser getUser() {
        return user;
    }

    public void setUser(ParcelableUser user) {
        this.user = user;
    }

    public final boolean isUser(@NonNull ParcelableUser user) {
        return userKey.equals(user.key);
    }

    @Override
    public String toString() {
        return "FriendshipTaskEvent{" +
                "action=" + action +
                ", finished=" + finished +
                ", succeeded=" + succeeded +
                ", accountKey=" + accountKey +
                ", userKey=" + userKey +
                ", user=" + user +
                '}';
    }

    @IntDef({Action.ACCEPT, Action.DENY, Action.FOLLOW, Action.UNFOLLOW, Action.BLOCK,
            Action.UNBLOCK, Action.MUTE, Action.UNMUTE, Action.FILTER, Action.UNFILTER})
    public @interface Action {
        int ACCEPT = 1;
        int DENY = 2;
        int FOLLOW = 3;
        int UNFOLLOW = 4;
        int BLOCK = 5;
        int UNBLOCK = 6;
        int MUTE = 7;
        int UNMUTE = 8;
        int FILTER = 9;
        int UNFILTER = 10;
    }
}
