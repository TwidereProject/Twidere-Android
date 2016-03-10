package org.mariotaku.twidere.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by mariotaku on 16/2/11.
 */
public class BaseRefreshTaskParam implements RefreshTaskParam {

    private final UserKey[] accountKeys;
    private final String[] maxIds;
    private final String[] sinceIds;
    private final long[] maxSortIds;
    private final long[] sinceSortIds;

    public BaseRefreshTaskParam(UserKey[] accountKeys, String[] maxIds, String[] sinceIds) {
        this(accountKeys, maxIds, sinceIds, null, null);
    }

    public BaseRefreshTaskParam(UserKey[] accountKeys, String[] maxIds, String[] sinceIds,
                                long[] maxSortIds, long[] sinceSortIds) {
        this.accountKeys = accountKeys;
        this.maxIds = maxIds;
        this.sinceIds = sinceIds;
        this.maxSortIds = maxSortIds;
        this.sinceSortIds = sinceSortIds;
    }

    @NonNull
    @Override
    public UserKey[] getAccountKeys() {
        return accountKeys;
    }

    @Nullable
    @Override
    public String[] getMaxIds() {
        return maxIds;
    }

    @Nullable
    @Override
    public String[] getSinceIds() {
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

    public long[] getMaxSortIds() {
        return maxSortIds;
    }

    public long[] getSinceSortIds() {
        return sinceSortIds;
    }

}
