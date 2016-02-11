package org.mariotaku.twidere.model;

/**
 * Created by mariotaku on 16/2/11.
 */
public class RefreshTaskParam {

    private final long[] accountIds, maxIds, sinceIds;

    public long[] getAccountIds() {
        return accountIds;
    }

    public long[] getMaxIds() {
        return maxIds;
    }

    public long[] getSinceIds() {
        return sinceIds;
    }

    public RefreshTaskParam(long[] accountIds, long[] maxIds, long[] sinceIds) {
        this.accountIds = accountIds;
        this.maxIds = maxIds;
        this.sinceIds = sinceIds;
    }
}
