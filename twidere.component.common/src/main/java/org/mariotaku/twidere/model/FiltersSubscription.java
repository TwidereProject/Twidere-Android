package org.mariotaku.twidere.model;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import org.mariotaku.twidere.provider.TwidereDataStore;
import org.mariotaku.twidere.provider.TwidereDataStore.Filters;

/**
 * Created by mariotaku on 2017/1/9.
 */

@CursorObject(valuesCreator = true, tableInfo = true)
public class FiltersSubscription {
    @CursorField(value = Filters.Subscriptions._ID, excludeWrite = true, type = TwidereDataStore.TYPE_PRIMARY_KEY)
    public long id;

    @CursorField(Filters.Subscriptions.COMPONENT)
    public String component;

    @CursorField(Filters.Subscriptions.ARGUMENTS)
    public String arguments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FiltersSubscription that = (FiltersSubscription) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "FiltersSubscription{" +
                "arguments='" + arguments + '\'' +
                ", id=" + id +
                ", component='" + component + '\'' +
                '}';
    }
}
