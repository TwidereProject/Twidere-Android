package org.mariotaku.twidere.model;

import android.support.annotation.Nullable;

/**
 * Created by mariotaku on 16/2/14.
 */
public abstract class SimpleRefreshTaskParam implements RefreshTaskParam {

    @Nullable
    @Override
    public String[] getMaxIds() {
        return null;
    }

    @Nullable
    @Override
    public String[] getSinceIds() {
        return null;
    }

    @Override
    public boolean hasMaxIds() {
        return getMaxIds() != null;
    }

    @Override
    public boolean hasSinceIds() {
        return getSinceIds() != null;
    }

    @Nullable
    @Override
    public long[] getSinceSortIds() {
        return null;
    }

    @Nullable
    @Override
    public long[] getMaxSortIds() {
        return null;
    }
}
