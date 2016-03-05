package org.mariotaku.twidere.model.message;

import org.mariotaku.twidere.model.AccountId;

/**
 * Created by mariotaku on 16/2/26.
 */
public class SavedSearchDestroyedEvent {
    private final AccountId accountId;
    private final long searchId;

    public SavedSearchDestroyedEvent(AccountId accountId, long searchId) {
        this.accountId = accountId;
        this.searchId = searchId;
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public long getSearchId() {
        return searchId;
    }
}
