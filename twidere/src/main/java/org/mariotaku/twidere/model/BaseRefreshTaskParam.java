package org.mariotaku.twidere.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by mariotaku on 16/2/11.
 */
public class BaseRefreshTaskParam implements RefreshTaskParam {

    private final org.mariotaku.twidere.model.AccountId[] accountIds;
    private final long[] maxIds;
    private final long[] sinceIds;

    @NonNull
    @Override
    public org.mariotaku.twidere.model.AccountId[] getAccountIds() {
        return accountIds;
    }

    @Nullable
    @Override
    public long[] getMaxIds() {
        return maxIds;
    }

    @Nullable
    @Override
    public long[] getSinceIds() {
        return sinceIds;
    }

    public BaseRefreshTaskParam(org.mariotaku.twidere.model.AccountId[] accountIds, long[] maxIds, long[] sinceIds) {
        this.accountIds = accountIds;
        this.maxIds = maxIds;
        this.sinceIds = sinceIds;
    }

}
