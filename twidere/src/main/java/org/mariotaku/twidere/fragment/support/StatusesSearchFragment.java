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

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.content.Loader;

import org.mariotaku.twidere.loader.support.TweetSearchLoader;
import org.mariotaku.twidere.model.ParcelableStatus;

import java.util.List;

/**
 * Created by mariotaku on 14/12/2.
 */
public class StatusesSearchFragment extends ParcelableStatusesFragment {

    @Override
    protected Loader<List<ParcelableStatus>> onCreateStatusesLoader(final Context context,
                                                                    final Bundle args,
                                                                    final boolean fromUser) {
        setRefreshing(true);
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final long maxId = args.getLong(EXTRA_MAX_ID, -1);
        final long sinceId = args.getLong(EXTRA_SINCE_ID, -1);
        final String query = args.getString(EXTRA_QUERY);
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        final boolean makeGap = args.getBoolean(EXTRA_MAKE_GAP, true);
        final boolean twitterOptimizedSearches = mPreferences.getBoolean(KEY_TWITTER_OPTIMIZED_SEARCHES);
        return new TweetSearchLoader(getActivity(), accountId, query, sinceId, maxId, getAdapterData(),
                getSavedStatusesFileArgs(), tabPosition, fromUser, makeGap, twitterOptimizedSearches);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        super.fitSystemWindows(insets);
    }

    @Override
    protected String[] getSavedStatusesFileArgs() {
        final Bundle args = getArguments();
        if (args == null) return null;
        final long account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final String query = args.getString(EXTRA_QUERY);
        return new String[]{AUTHORITY_SEARCH_TWEETS, "account" + account_id, "query" + query};
    }


}
