/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import org.mariotaku.twidere.api.MicroBlog;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ScheduledStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;

import java.util.List;

/**
 * Created by mariotaku on 15/7/10.
 */
public class ScheduledStatusesLoader extends AsyncTaskLoader<List<ScheduledStatus>> {

    private final UserKey mAccountId;
    private final String mSinceId;
    private final String mMaxId;
    @ScheduledStatus.State
    private final String[] mStates;

    public ScheduledStatusesLoader(Context context, UserKey accountId, String sinceId, String maxId,
                                   @ScheduledStatus.State String[] states, List<ScheduledStatus> data) {
        super(context);
        mAccountId = accountId;
        mSinceId = sinceId;
        mMaxId = maxId;
        mStates = states;
    }


    @Override
    public List<ScheduledStatus> loadInBackground() {
        final MicroBlog twitter = MicroBlogAPIFactory.getTwitterInstance(getContext(), mAccountId, true);
        if (twitter == null) return null;
        final Paging paging = new Paging();
        if (mSinceId != null) {
            paging.setSinceId(mSinceId);
        }
        if (mMaxId != null) {
            paging.setMaxId(mMaxId);
        }
        try {
            return twitter.getScheduledStatuses(paging, mStates);
        } catch (TwitterException e) {
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
