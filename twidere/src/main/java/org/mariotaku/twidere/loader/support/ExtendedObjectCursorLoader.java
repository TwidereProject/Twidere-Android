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

package org.mariotaku.twidere.loader.support;

import android.content.Context;
import android.net.Uri;

import org.mariotaku.twidere.loader.iface.IExtendedLoader;
import org.mariotaku.twidere.model.ObjectCursor;

/**
 * Created by mariotaku on 15/1/9.
 */
public class ExtendedObjectCursorLoader<E> extends ObjectCursorLoader<E> implements IExtendedLoader {

    public ExtendedObjectCursorLoader(Context context, Class<? extends ObjectCursor.CursorIndices<E>> indicesClass) {
        super(context, indicesClass);
    }

    public ExtendedObjectCursorLoader(Context context, Class<? extends ObjectCursor.CursorIndices<E>> indicesClass,
                                      Uri uri, String[] projection, String selection, String[] selectionArgs,
                                      String sortOrder, boolean fromUser) {
        super(context, indicesClass, uri, projection, selection, selectionArgs, sortOrder);
        setFromUser(fromUser);
    }

    @Override
    public boolean isFromUser() {
        return true;
    }

    @Override
    public void setFromUser(boolean fromUser) {
        //No-op
    }

}
