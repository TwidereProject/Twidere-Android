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

package org.mariotaku.twidere.model;


import android.database.Cursor;
import androidx.annotation.NonNull;

import java.io.Closeable;
import java.io.IOException;

public class CursorReference<C extends Cursor> implements Closeable {

    @NonNull
    private final C cursor;

    private CursorReference(@NonNull C cursor) {
        this.cursor = cursor;
    }

    @NonNull
    public C get() {
        return cursor;
    }

    @SuppressWarnings("unused")
    @NonNull
    public C component1() {
        return get();
    }

    @Override
    public void close() throws IOException {
        cursor.close();
    }

    public static <Cur extends Cursor> CursorReference<Cur> get(Cur cursor) {
        return new CursorReference<>(cursor);
    }
}
