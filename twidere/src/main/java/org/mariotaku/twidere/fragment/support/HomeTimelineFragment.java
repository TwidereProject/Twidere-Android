/*
 * Twidere - Twitter client for Android
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

import android.content.SharedPreferences;
import android.net.Uri;

import org.mariotaku.twidere.provider.TwidereDataStore.Statuses;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;

/**
 * Created by mariotaku on 14/12/3.
 */
public class HomeTimelineFragment extends CursorStatusesFragment {

    @Override
    public Uri getContentUri() {
        return Statuses.CONTENT_URI;
    }

    @Override
    protected void updateRefreshState() {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        if (twitter == null) return;
        setRefreshing(twitter.isHomeTimelineRefreshing());
    }

    @Override
    protected int getNotificationType() {
        return NOTIFICATION_ID_HOME_TIMELINE;
    }

    @Override
    protected boolean isFilterEnabled() {
        final SharedPreferences pref = getSharedPreferences();
        return pref != null && pref.getBoolean(KEY_FILTERS_IN_HOME_TIMELINE, true);
    }

    @Override
    public int getStatuses(long[] accountIds, long[] maxIds, long[] sinceIds) {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        if (twitter == null) return -1;
        if (maxIds == null) {
            return twitter.refreshAll(accountIds);
        }
        return twitter.getHomeTimelineAsync(accountIds, maxIds, sinceIds);
    }

}
