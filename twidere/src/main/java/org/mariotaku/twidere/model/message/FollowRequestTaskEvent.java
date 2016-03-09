package org.mariotaku.twidere.model.message;

import android.support.annotation.IntDef;

import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 16/2/15.
 */
public class FollowRequestTaskEvent {

    @Action
    private int action;
    private boolean finished;
    private boolean succeeded;
    private UserKey mAccountKey;
    private String userId;

    public FollowRequestTaskEvent(@Action int action, UserKey accountKey, String userId) {
        this.action = action;
        this.mAccountKey = accountKey;
        this.userId = userId;
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

    public UserKey getAccountKey() {
        return mAccountKey;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    @Override
    public String toString() {
        return "FollowRequestTaskEvent{" +
                "action=" + action +
                ", finished=" + finished +
                ", mAccountKey=" + mAccountKey +
                ", userId=" + userId +
                '}';
    }

    @IntDef({Action.ACCEPT, Action.DENY})
    public @interface Action {
        int ACCEPT = 1;
        int DENY = 2;
    }
}
