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
import android.support.v4.content.Loader;

import org.mariotaku.twidere.adapter.iface.IStatusesListAdapter;
import org.mariotaku.twidere.loader.support.UserTimelineLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import java.util.List;

import static org.mariotaku.twidere.util.Utils.getAccountId;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;

public class UserTimelineFragmentOld extends ParcelableStatusesListFragment {

    @Override
    public Loader<List<ParcelableStatus>> newLoaderInstance(final Context context, final Bundle args) {
        if (args == null) return null;
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final long maxId = args.getLong(EXTRA_MAX_ID, -1);
        final long sinceId = args.getLong(EXTRA_SINCE_ID, -1);
        final long userId = args.getLong(EXTRA_USER_ID, -1);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        return new UserTimelineLoader(context, accountId, userId, screenName, maxId, sinceId, getData(),
                getSavedStatusesFileArgs(), tabPosition);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Bundle args = getArguments();
        final long accountId = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
        final long userId = args != null ? args.getLong(EXTRA_USER_ID, -1) : -1;
        final String screenName = args != null ? args.getString(EXTRA_SCREEN_NAME) : null;
        final boolean isMyTimeline = userId > 0 ? accountId == userId : accountId == getAccountId(getActivity(),
                screenName);
        final IStatusesListAdapter<List<ParcelableStatus>> adapter = getListAdapter();
        adapter.setIndicateMyStatusDisabled(isMyTimeline);
        adapter.setFiltersEnabled(!isMyTimeline);
        adapter.setIgnoredFilterFields(true, false, false, false, false);
    }

    @Override
    protected String[] getSavedStatusesFileArgs() {
        final Bundle args = getArguments();
        if (args == null) return null;
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final long userId = args.getLong(EXTRA_USER_ID, -1);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        return new String[]{AUTHORITY_USER_TIMELINE, "account" + accountId, "user" + userId + "name" + screenName};
    }

    @Override
    protected boolean isMyTimeline() {
        final Bundle args = getArguments();
        if (args != null) {
            final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
            final long userId = args.getLong(EXTRA_USER_ID, -1);
            final String screenName = args.getString(EXTRA_SCREEN_NAME);
            if (accountId == userId || screenName != null
                    && screenName.equals(getAccountScreenName(getActivity(), accountId)))
                return true;
        }
        return false;
    }

    @Override
    protected boolean shouldShowAccountColor() {
        return false;
    }

}
