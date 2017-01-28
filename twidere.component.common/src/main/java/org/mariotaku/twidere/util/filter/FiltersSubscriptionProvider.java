package org.mariotaku.twidere.util.filter;

import android.support.annotation.Nullable;

import org.mariotaku.twidere.model.FiltersData;

import java.io.IOException;
import java.util.List;

/**
 * Created by mariotaku on 2017/1/9.
 */

public interface FiltersSubscriptionProvider {
    boolean fetchFilters() throws IOException;

    boolean firstAdded();

    boolean deleteLocalData();

    @Nullable
    List<FiltersData.UserItem> getUsers();

    @Nullable
    List<FiltersData.BaseItem> getKeywords();

    @Nullable
    List<FiltersData.BaseItem> getSources();

    @Nullable
    List<FiltersData.BaseItem> getLinks();
}
