package org.mariotaku.twidere.model.message;

import org.mariotaku.twidere.model.AccountKey;

/**
 * Created by mariotaku on 16/3/7.
 */
public class UsersBlockedEvent {
    private AccountKey accountKey;
    private long[] userIds;

    public UsersBlockedEvent(AccountKey accountKey, long[] userIds) {
        this.accountKey = accountKey;
        this.userIds = userIds;
    }

    public AccountKey getAccountKey() {
        return accountKey;
    }

    public long[] getUserIds() {
        return userIds;
    }
}
