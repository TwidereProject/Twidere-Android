/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.model;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

public class ListResponse<Data> extends AbstractList<Data> implements Response<List<Data>>, List<Data> {

    @Nullable
    private final List<Data> list;
    private final Exception exception;
    private final Bundle extras;

    public ListResponse(final Exception exception) {
        this(null, exception, new Bundle());
    }

    public ListResponse(final List<Data> list) {
        this(list, null, new Bundle());
    }

    public ListResponse(final List<Data> list, final Exception exception) {
        this(list, exception, new Bundle());
    }

    public ListResponse(@Nullable final List<Data> list, final Exception exception, @NonNull final Bundle extras) {
        this.list = list;
        this.exception = exception;
        this.extras = extras;
    }

    public static <Data> ListResponse<Data> getListInstance(Exception exception) {
        return new ListResponse<>(null, exception);
    }

    public static <Data> ListResponse<Data> getListInstance(List<Data> data) {
        return new ListResponse<>(data, null);
    }

    public static <Data> ListResponse<Data> emptyListInstance() {
        return new ListResponse<>(Collections.emptyList(), null);
    }

    public static <Data> ListResponse<Data> getListInstance(List<Data> list, Exception e) {
        return new ListResponse<>(list, e);
    }

    @Override
    public int size() {
        if (list == null) return 0;
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list == null || list.isEmpty();
    }

    @Override
    public Data remove(int location) {
        if (list == null) return null;
        return list.remove(location);
    }

    @Override
    public Data set(int location, Data object) {
        if (list == null) return null;
        return list.set(location, object);
    }

    @Override
    public void add(int location, Data object) {
        if (list == null) return;
        list.add(location, object);
    }

    @Override
    public Data get(int location) {
        if (list == null) return null;
        return list.get(location);
    }

    @Override
    public List<Data> getData() {
        return list;
    }

    @Override
    public Exception getException() {
        return exception;
    }

    @NonNull
    @Override
    public Bundle getExtras() {
        return extras;
    }

    @Override
    public boolean hasData() {
        return list != null;
    }

    @Override
    public boolean hasException() {
        return exception != null;
    }
}
