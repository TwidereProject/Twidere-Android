package org.mariotaku.twidere.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by mariotaku on 16/2/11.
 */
public class BaseRefreshTaskParam implements RefreshTaskParam {

    private final UserKey[] accountKeys;
    private final long[] maxIds;
    private final long[] sinceIds;

    @NonNull
    @Override
    public UserKey[] getAccountKeys() {
        return accountKeys;
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

    @Override
    public boolean hasMaxIds() {
        return maxIds != null;
    }

    @Override
    public boolean hasSinceIds() {
        return sinceIds != null;
    }

    public BaseRefreshTaskParam(UserKey[] accountKeys, long[] maxIds, long[] sinceIds) {
        this.accountKeys = accountKeys;
        this.maxIds = maxIds;
        this.sinceIds = sinceIds;
    }

}
