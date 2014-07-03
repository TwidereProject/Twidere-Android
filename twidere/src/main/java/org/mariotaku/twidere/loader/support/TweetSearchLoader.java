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

import static org.mariotaku.twidere.util.Utils.isFiltered;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.mariotaku.twidere.model.ParcelableStatus;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.Arrays;
import java.util.List;

public class TweetSearchLoader extends Twitter4JStatusesLoader {

	private final String mQuery;
	private final boolean mFiltersForRts;

	public TweetSearchLoader(final Context context, final long accountId, final String query, final long maxId,
			final long sinceId, final List<ParcelableStatus> data, final String[] savedStatusesArgs,
			final int tabPosition) {
		super(context, accountId, maxId, sinceId, data, savedStatusesArgs, tabPosition);
		mQuery = query;
		mFiltersForRts = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getBoolean(
				KEY_FILTERS_FOR_RTS, true);
	}

	@Override
	public List<Status> getStatuses(final Twitter twitter, final Paging paging) throws TwitterException {
		if (twitter == null) return null;
		final Query query = new Query(processQuery(mQuery));
		query.setRpp(paging.getCount());
		if (paging.getMaxId() > 0) {
			query.setMaxId(paging.getMaxId());
		}
		return Arrays.asList(twitter.search(query).getStatuses());
	}

	protected String processQuery(final String query) {
		return String.format("%s", query);
	}

	@Override
	protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
		return isFiltered(database, status, mFiltersForRts);
	}

}
