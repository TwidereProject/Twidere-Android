package org.mariotaku.twidere.model.message;

import org.mariotaku.twidere.model.UserKey;

/**
 * Created by mariotaku on 16/3/7.
 */
public class UsersBlockedEvent {
    private UserKey accountKey;
    private String[] userIds;

    public UsersBlockedEvent(UserKey accountKey, String[] userIds) {
        this.accountKey = accountKey;
        this.userIds = userIds;
    }

    public UserKey getAccountKey() {
        return accountKey;
    }

    public String[] getUserIds() {
        return userIds;
    }
}
