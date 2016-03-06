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

package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import org.mariotaku.twidere.loader.support.IDsUsersLoader;
import org.mariotaku.twidere.loader.support.StatusRetweetersLoader;
import org.mariotaku.twidere.model.AccountKey;

public class StatusRetweetersListFragment extends CursorSupportUsersListFragment {

	@Override
	public IDsUsersLoader onCreateUsersLoader(final Context context, @NonNull final Bundle args, boolean fromUser) {
		final AccountKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
		final long statusId = args.getLong(EXTRA_STATUS_ID, -1);
		final StatusRetweetersLoader loader = new StatusRetweetersLoader(context, accountKey, statusId,
				getData(), fromUser);
		loader.setCursor(getNextCursor());
		return loader;
	}

}
