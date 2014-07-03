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

package org.mariotaku.twidere.loader.support;

import android.content.Context;
import android.os.Bundle;

import org.mariotaku.twidere.model.ParcelableUser;

import java.util.Collections;
import java.util.List;

public class IntentExtrasUsersLoader extends ParcelableUsersLoader {

	private final Bundle mExtras;

	public IntentExtrasUsersLoader(final Context context, final Bundle extras, final List<ParcelableUser> data) {
		super(context, data);
		mExtras = extras;
	}

	@Override
	public List<ParcelableUser> loadInBackground() {
		final List<ParcelableUser> data = getData();
		if (mExtras != null) {
			final List<ParcelableUser> users = mExtras.getParcelableArrayList(EXTRA_USERS);
			if (users != null) {
				data.addAll(users);
				Collections.sort(data);
			}
		}
		return data;
	}

}
