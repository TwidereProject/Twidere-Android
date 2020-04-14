/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.model.pagination;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by mariotaku on 2017/4/21.
 */

public class PaginatedArrayList<E> extends ArrayList<E> implements PaginatedList<E> {

    private Pagination previousPage;
    private Pagination nextPage;

    public PaginatedArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    public PaginatedArrayList() {
    }

    public PaginatedArrayList(@NonNull Collection<? extends E> c) {
        super(c);
    }

    @Override
    public Pagination getPreviousPage() {
        return previousPage;
    }

    @Override
    public Pagination getNextPage() {
        return nextPage;
    }

    public void setPreviousPage(Pagination previousPage) {
        this.previousPage = previousPage;
    }

    public void setNextPage(Pagination nextPage) {
        this.nextPage = nextPage;
    }
}
