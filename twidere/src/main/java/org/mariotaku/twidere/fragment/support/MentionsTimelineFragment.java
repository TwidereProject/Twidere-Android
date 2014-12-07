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

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.message.TaskStateChangedEvent;

/**
 * Created by mariotaku on 14/12/3.
 */
public class MentionsTimelineFragment extends CursorStatusesFragment {

    @Override
    public Uri getContentUri() {
        return Mentions.CONTENT_URI;
    }

    @Override
    protected int getNotificationType() {
        return NOTIFICATION_ID_MENTIONS_TIMELINE;
    }

    @Override
    protected boolean isFilterEnabled() {
        final SharedPreferences pref = getSharedPreferences();
        return pref != null && pref.getBoolean(KEY_FILTERS_IN_MENTIONS_TIMELINE, true);
    }

    @Override
    public int getStatuses(long[] accountIds, long[] maxIds, long[] sinceIds) {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        if (twitter == null) return -1;
        return twitter.getMentionsTimelineAsync(accountIds, maxIds, sinceIds);
    }

    @Override
    public void onStart() {
        super.onStart();
        final Bus bus = TwidereApplication.getInstance(getActivity()).getMessageBus();
        bus.register(this);
    }

    @Override
    public void onStop() {
        final Bus bus = TwidereApplication.getInstance(getActivity()).getMessageBus();
        bus.unregister(this);
        super.onStop();
    }

    @Subscribe
    public void notifyTaskStateChanged(TaskStateChangedEvent event) {
        updateRefreshState();
    }

    @Override
    protected void onReceivedBroadcast(Intent intent, String action) {
    }

    @Override
    protected void onSetIntentFilter(IntentFilter filter) {
    }

    private void updateRefreshState() {
        final AsyncTwitterWrapper twitter = getTwitterWrapper();
        if (twitter == null) return;
        setRefreshing(twitter.isMentionsTimelineRefreshing());
    }

}
