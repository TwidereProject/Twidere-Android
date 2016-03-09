package org.mariotaku.twidere.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by mariotaku on 16/2/14.
 */
public interface RefreshTaskParam {
    @NonNull
    UserKey[] getAccountKeys();

    @Nullable
    String[] getMaxIds();

    @Nullable
    String[] getSinceIds();

    boolean hasMaxIds();

    boolean hasSinceIds();

}
