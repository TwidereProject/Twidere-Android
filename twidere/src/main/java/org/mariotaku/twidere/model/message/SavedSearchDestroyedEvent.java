package org.mariotaku.twidere.model.message;

/**
 * Created by mariotaku on 16/2/26.
 */
public class SavedSearchDestroyedEvent {
    private final long accountId, searchId;

    public SavedSearchDestroyedEvent(long accountId, long searchId) {
        this.accountId = accountId;
        this.searchId = searchId;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getSearchId() {
        return searchId;
    }
}
