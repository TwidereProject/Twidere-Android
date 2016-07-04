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

package org.mariotaku.twidere.loader;

import android.content.Context;
import android.os.Bundle;

import org.mariotaku.twidere.model.ListResponse;
import org.mariotaku.twidere.model.ParcelableStatus;

import java.util.Collections;
import java.util.List;

import static org.mariotaku.twidere.constant.IntentConstants.EXTRA_STATUSES;

public class IntentExtrasStatusesLoader extends ParcelableStatusesLoader {

    private final Bundle mExtras;

    public IntentExtrasStatusesLoader(final Context context, final Bundle extras,
                                      final List<ParcelableStatus> data, final boolean fromUser) {
        super(context, data, -1, fromUser);
        mExtras = extras;
    }

    @Override
    public ListResponse<ParcelableStatus> loadInBackground() {
        final List<ParcelableStatus> data = getData();
        if (mExtras != null && mExtras.containsKey(EXTRA_STATUSES)) {
            final List<ParcelableStatus> users = mExtras.getParcelableArrayList(EXTRA_STATUSES);
            if (data != null && users != null) {
                data.addAll(users);
                Collections.sort(data);
            }
        }
        return ListResponse.getListInstance(data);
    }

}
