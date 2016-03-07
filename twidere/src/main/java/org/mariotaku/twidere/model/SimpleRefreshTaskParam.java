package org.mariotaku.twidere.model;

import android.support.annotation.Nullable;

/**
 * Created by mariotaku on 16/2/14.
 */
public abstract class SimpleRefreshTaskParam implements RefreshTaskParam {

    @Nullable
    @Override
    public long[] getMaxIds() {
        return null;
    }

    @Nullable
    @Override
    public long[] getSinceIds() {
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
}
