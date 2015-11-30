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

import android.net.Uri;

import org.mariotaku.twidere.provider.TwidereDataStore;

public class ActivitiesByFriendsFragment extends CursorActivitiesFragment {


    @Override
    public boolean getActivities(long[] accountIds, long[] maxIds, long[] sinceIds) {
        return false;
    }


    @Override
    public Uri getContentUri() {
        return TwidereDataStore.CONTENT_URI_EMPTY;
    }

    @Override
    protected int getNotificationType() {
        return 0;
    }

    @Override
    protected boolean isFilterEnabled() {
        return false;
    }

    @Override
    protected void updateRefreshState() {

    }

    @Override
    public boolean isRefreshing() {
        return false;
    }

}
