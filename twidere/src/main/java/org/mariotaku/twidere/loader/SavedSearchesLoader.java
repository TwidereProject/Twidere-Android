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

package org.mariotaku.twidere.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.model.ResponseList;
import org.mariotaku.microblog.library.twitter.model.SavedSearch;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;

public class SavedSearchesLoader extends AsyncTaskLoader<ResponseList<SavedSearch>> implements Constants {

    private final UserKey mAccountId;

    public SavedSearchesLoader(final Context context, final UserKey accountKey) {
        super(context);
        mAccountId = accountKey;
    }

    @Override
    public ResponseList<SavedSearch> loadInBackground() {
        final MicroBlog twitter = MicroBlogAPIFactory.getInstance(getContext(), mAccountId,
                false);
        if (twitter == null) return null;
        try {
            return twitter.getSavedSearches();
        } catch (final MicroBlogException e) {
            Log.w(LOGTAG, e);
        }
        return null;
    }

    @Override
    public void onStartLoading() {
        forceLoad();
    }

}
