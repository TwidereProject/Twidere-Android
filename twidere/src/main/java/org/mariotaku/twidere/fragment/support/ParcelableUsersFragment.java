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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import org.mariotaku.twidere.adapter.ParcelableUsersAdapter;
import org.mariotaku.twidere.model.ParcelableUser;

import java.util.List;

public abstract class ParcelableUsersFragment extends AbsUsersFragment<List<ParcelableUser>> {

    @Override
    public boolean isRefreshing() {
        final LoaderManager lm = getLoaderManager();
        return lm.hasRunningLoaders();
    }

    @NonNull
    @Override
    protected final ParcelableUsersAdapter onCreateAdapter(Context context, boolean compact) {
        return new ParcelableUsersAdapter(context, compact);
    }

    protected long getAccountId() {
        final Bundle args = getArguments();
        return args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
    }

    @Override
    protected boolean hasMoreData(List<ParcelableUser> data) {
        return data == null || !data.isEmpty();
    }

    @Override
    public void onLoadFinished(Loader<List<ParcelableUser>> loader, List<ParcelableUser> data) {
        super.onLoadFinished(loader, data);
        setRefreshEnabled(true);
        setRefreshing(false);
        setLoadMoreIndicatorVisible(false);
    }

    protected void removeUsers(long... ids) {
        //TODO remove from adapter
    }

}
