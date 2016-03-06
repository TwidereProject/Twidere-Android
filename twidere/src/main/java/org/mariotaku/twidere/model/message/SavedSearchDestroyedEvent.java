package org.mariotaku.twidere.model.message;

import org.mariotaku.twidere.model.AccountKey;

/**
 * Created by mariotaku on 16/2/26.
 */
public class SavedSearchDestroyedEvent {
    private final AccountKey mAccountKey;
    private final long searchId;

    public SavedSearchDestroyedEvent(AccountKey accountKey, long searchId) {
        this.mAccountKey = accountKey;
        this.searchId = searchId;
    }

    public AccountKey getAccountKey() {
        return mAccountKey;
    }

    public long getSearchId() {
        return searchId;
    }
}
