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
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.ResponseList;
import org.mariotaku.twidere.api.twitter.model.SavedSearch;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.TwitterAPIFactory;

import static org.mariotaku.twidere.TwidereConstants.LOGTAG;

public class SavedSearchesLoader extends AsyncTaskLoader<ResponseList<SavedSearch>> {

    private final UserKey mAccountId;

    public SavedSearchesLoader(final Context context, final UserKey accountKey) {
        super(context);
        mAccountId = accountKey;
    }

    @Override
    public ResponseList<SavedSearch> loadInBackground() {
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getContext(), mAccountId,
                false);
        if (twitter == null) return null;
        try {
            return twitter.getSavedSearches();
        } catch (final TwitterException e) {
            Log.w(LOGTAG, e);
        }
        return null;
    }

    @Override
    public void onStartLoading() {
        forceLoad();
    }

}
