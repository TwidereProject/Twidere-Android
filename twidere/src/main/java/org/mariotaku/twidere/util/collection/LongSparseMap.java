/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.util.collection;

import android.support.v4.util.LongSparseArray;

import java.util.HashSet;

/**
 * Created by mariotaku on 14/12/12.
 */
public class LongSparseMap<T> {

    private final LongSparseArray<HashSet<T>> internalArray;

    public LongSparseMap() {
        internalArray = new LongSparseArray<>();
    }

    public boolean put(long key, T value) {
        final int idx = internalArray.indexOfKey(key);
        final HashSet<T> set;
        if (idx < 0) {
            set = new HashSet<>();
            internalArray.put(key, set);
        } else {
            set = internalArray.valueAt(idx);
        }
        return set.add(value);
    }

    public boolean clear(long key) {
        final int idx = internalArray.indexOfKey(key);
        if (idx < 0) return false;
        internalArray.valueAt(idx).clear();
        return true;
    }

    public boolean remove(long key, T value) {
        final int idx = internalArray.indexOfKey(key);
        return idx >= 0 && internalArray.valueAt(idx).remove(value);
    }

    public boolean has(long key, T value) {
        final int idx = internalArray.indexOfKey(key);
        return idx >= 0 && internalArray.valueAt(idx).contains(value);
    }

}
