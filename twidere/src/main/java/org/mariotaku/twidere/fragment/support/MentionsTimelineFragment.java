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

import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.provider.TwidereDataStore.Mentions;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;

/**
 * Created by mariotaku on 14/12/3.
 */
public class MentionsTimelineFragment extends CursorStatusesFragment {

    @Override
    public Uri getContentUri() {
        return Mentions.CONTENT_URI;
    }

    @NonNull
    @Override
    protected ParcelableStatusesAdapter onCreateAdapter(Context context, boolean compact) {
        final ParcelableStatusesAdapter adapter = super.onCreateAdapter(context, compact);
        adapter.setShowInReplyTo(false);
        return adapter;
    }

    @Override
    public boolean isRefreshing() {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        return twitter != null && twitter.isMentionsTimelineRefreshing();
    }

    @Override
    protected int getNotificationType() {
        return NOTIFICATION_ID_MENTIONS_TIMELINE;
    }

    @Override
    protected boolean isFilterEnabled() {
        return true;
    }

    @Override
    protected void updateRefreshState() {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        if (twitter == null) return;
        setRefreshing(twitter.isMentionsTimelineRefreshing());
    }

    @Override
    public boolean getStatuses(long[] accountIds, long[] maxIds, long[] sinceIds) {
        final AsyncTwitterWrapper twitter = mTwitterWrapper;
        if (twitter == null) return false;
        return twitter.getMentionsTimelineAsync(accountIds, maxIds, sinceIds);
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        final FragmentActivity activity = getActivity();
        if (isVisibleToUser && activity != null) {
            final NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            for (long accountId : getAccountIds()) {
                final String tag = "mentions_" + accountId;
                nm.cancel(tag, NOTIFICATION_ID_MENTIONS_TIMELINE);
            }
        }
    }

    @Override
    protected String getReadPositionTag() {
        return TAB_TYPE_MENTIONS_TIMELINE;
    }

}
