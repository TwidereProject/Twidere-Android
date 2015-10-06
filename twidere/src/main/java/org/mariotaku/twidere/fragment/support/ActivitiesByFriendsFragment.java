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

import android.os.Bundle;
import android.support.v4.content.Loader;

import org.mariotaku.twidere.loader.support.ActivitiesByFriendsLoader;
import org.mariotaku.twidere.model.ParcelableActivity;

import java.util.List;

public class ActivitiesByFriendsFragment extends ParcelableActivitiesFragment {


    @Override
    public Loader<List<ParcelableActivity>> onCreateLoader(final int id, final Bundle args) {
        setProgressBarIndeterminateVisibility(true);
        final long[] accountIds = args.getLongArray(EXTRA_ACCOUNT_IDS);
        final long[] sinceIds = args.getLongArray(EXTRA_SINCE_IDS);
        final long[] maxIds = args.getLongArray(EXTRA_MAX_IDS);
        final long accountId = accountIds != null ? accountIds[0] : -1;
        final long sinceId = sinceIds != null ? sinceIds[0] : -1;
        final long maxId = maxIds != null ? maxIds[0] : -1;
        return new ActivitiesByFriendsLoader(getActivity(), accountId, sinceId, maxId, getAdapterData(),
                getSavedActivitiesFileArgs(), getTabPosition());
    }

    @Override
    protected boolean isByFriends() {
        return true;
    }

    @Override
    protected String[] getSavedActivitiesFileArgs() {
        final Bundle args = getArguments();
        if (args != null && args.containsKey(EXTRA_ACCOUNT_ID)) {
            final long account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
            return new String[]{AUTHORITY_ACTIVITIES_BY_FRIENDS, "account" + account_id};
        }
        return new String[]{AUTHORITY_ACTIVITIES_BY_FRIENDS};
    }

}
