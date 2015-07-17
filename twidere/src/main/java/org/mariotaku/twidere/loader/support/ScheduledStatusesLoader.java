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

package org.mariotaku.twidere.loader.support;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.ScheduledStatus;
import org.mariotaku.twidere.util.TwitterAPIFactory;

import java.util.List;

/**
 * Created by mariotaku on 15/7/10.
 */
public class ScheduledStatusesLoader extends AsyncTaskLoader<List<ScheduledStatus>> {

    private final long mAccountId;
    private final long mSinceId;
    private final long mMaxId;
    private final ScheduledStatus.State[] mStates;

    public ScheduledStatusesLoader(Context context, long accountId, long sinceId, long maxId, ScheduledStatus.State[] states, List<ScheduledStatus> data) {
        super(context);
        mAccountId = accountId;
        mSinceId = sinceId;
        mMaxId = maxId;
        mStates = states;
    }


    @Override
    public List<ScheduledStatus> loadInBackground() {
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getContext(), mAccountId, true);
        final Paging paging = new Paging();
        if (mSinceId > 0) {
            paging.setSinceId(mSinceId);
        }
        if (mMaxId > 0) {
            paging.setMaxId(mMaxId);
        }
        try {
            return twitter.getScheduledStatusesList(paging, mStates);
        } catch (TwitterException e) {
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
