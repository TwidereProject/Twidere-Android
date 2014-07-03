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

import org.mariotaku.twidere.model.ParcelableStatus;

import java.util.List;

public class UserMentionsLoader extends TweetSearchLoader {

	public UserMentionsLoader(final Context context, final long accountId, final String screenName, final long maxId,
			final long sinceId, final List<ParcelableStatus> data, final String[] savedStatusesArgs,
			final int tabPosition) {
		super(context, accountId, screenName, maxId, sinceId, data, savedStatusesArgs, tabPosition);
	}

	@Override
	protected String processQuery(final String query) {
		if (query == null) return null;
		final String screenName = query.startsWith("@") ? query : String.format("@%s", query);
		return String.format("%s exclude:retweets", screenName);
	}

}
