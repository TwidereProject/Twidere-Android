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

package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.adapter.ParcelableActivitiesAdapter;
import org.mariotaku.twidere.annotation.ReadPositionTag;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.provider.TwidereDataStore.Activities;
import org.mariotaku.twidere.util.ErrorInfoStore;

import edu.tsinghua.hotmobi.model.TimelineType;

public class ActivitiesAboutMeFragment extends CursorActivitiesFragment {

    @Override
    public boolean getActivities(long[] accountIds, long[] maxIds, long[] sinceIds) {
        mTwitterWrapper.getActivitiesAboutMeAsync(accountIds, maxIds, sinceIds);
        return true;
    }

    @NonNull
    @Override
    @TimelineType
    protected String getTimelineType() {
        return TimelineType.INTERACTIONS;
    }

    @NonNull
    @Override
    protected String getErrorInfoKey() {
        return ErrorInfoStore.KEY_INTERACTIONS;
    }

    @Override
    public Uri getContentUri() {
        return Activities.AboutMe.CONTENT_URI;
    }

    @Override
    protected int getNotificationType() {
        return NOTIFICATION_ID_INTERACTIONS_TIMELINE;
    }

    @Override
    protected boolean isFilterEnabled() {
        return true;
    }

    @Override
    protected void updateRefreshState() {
    }

    @Override
    @NonNull
    protected Where processWhere(@NonNull Expression where, @NonNull String[] whereArgs) {
        final Bundle arguments = getArguments();
        if (arguments != null) {
            final Bundle extras = arguments.getBundle(EXTRA_EXTRAS);
            if (extras != null && extras.getBoolean(EXTRA_MENTIONS_ONLY)) {
                final Expression expression = Expression.and(where, Expression.inArgs(Activities.ACTION, 3));
                return new Where(expression, ArrayUtils.addAll(whereArgs, Activity.Action.MENTION,
                        Activity.Action.REPLY, Activity.Action.QUOTE));
            }
        }
        return super.processWhere(where, whereArgs);
    }

    @NonNull
    @Override
    protected ParcelableActivitiesAdapter onCreateAdapter(Context context, boolean compact) {
        final ParcelableActivitiesAdapter adapter = new ParcelableActivitiesAdapter(context, compact, false);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            final Bundle extras = arguments.getBundle(EXTRA_EXTRAS);
            if (extras != null) {
                adapter.setFollowingOnly(extras.getBoolean(EXTRA_MY_FOLLOWING_ONLY));
                adapter.setMentionsOnly(extras.getBoolean(EXTRA_MENTIONS_ONLY));
            }
        }
        return adapter;
    }

    @Nullable
    @Override
    @ReadPositionTag
    protected String getReadPositionTag() {
        return ReadPositionTag.ACTIVITIES_ABOUT_ME;
    }

    @Override
    public boolean isRefreshing() {
        return false;
    }

}
