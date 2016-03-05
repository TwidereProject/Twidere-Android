package org.mariotaku.twidere.model.message;

import android.support.annotation.IntDef;

import org.mariotaku.twidere.model.AccountId;

/**
 * Created by mariotaku on 16/2/15.
 */
public class FollowRequestTaskEvent {

    @Action
    private int action;
    private boolean finished;
    private boolean succeeded;
    private AccountId accountId;
    private long userId;

    public FollowRequestTaskEvent(@Action int action, AccountId accountId, long userId) {
        this.action = action;
        this.accountId = accountId;
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

    public AccountId getAccountId() {
        return accountId;
    }

    public long getUserId() {
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
                ", accountId=" + accountId +
                ", userId=" + userId +
                '}';
    }

    @IntDef({Action.ACCEPT, Action.DENY})
    public @interface Action {
        int ACCEPT = 1;
        int DENY = 2;
    }
}
