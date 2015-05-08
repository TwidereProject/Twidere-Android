/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.api.twitter.model.impl;

import java.util.Collection;

import org.mariotaku.twidere.api.twitter.model.PageableResponseList;

/**
 * Created by mariotaku on 15/5/7.
 */
public class PageableResponseListImpl<T> extends ResponseListImpl<T> implements PageableResponseList<T> {

    long previousCursor;
    long nextCursor;


    public PageableResponseListImpl(int capacity) {
        super(capacity);
    }

    public PageableResponseListImpl() {
    }

    public PageableResponseListImpl(Collection<? extends T> collection) {
        super(collection);
    }

    @Override
    public long getNextCursor() {
        return nextCursor;
    }

    @Override
    public long getPreviousCursor() {
        return previousCursor;
    }

    @Override
    public boolean hasNext() {
        return nextCursor != 0;
    }

    @Override
    public boolean hasPrevious() {
        return previousCursor != 0;
    }
}
