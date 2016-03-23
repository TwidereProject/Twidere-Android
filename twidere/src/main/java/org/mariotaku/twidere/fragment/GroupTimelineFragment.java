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

package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ComposeActivity;
import org.mariotaku.twidere.loader.GroupTimelineLoader;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.Utils;

import java.util.List;

import edu.tsinghua.hotmobi.model.TimelineType;

/**
 * Created by mariotaku on 14/12/2.
 */
public class GroupTimelineFragment extends ParcelableStatusesFragment {
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_group_timeline, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.compose: {
                final Bundle args = getArguments();
                final UserKey accountKey = Utils.getAccountKey(getContext(), args);
                final String groupName = args.getString(EXTRA_GROUP_NAME);
                if (groupName != null) {
                    final Intent intent = new Intent(getActivity(), ComposeActivity.class);
                    intent.setAction(INTENT_ACTION_COMPOSE);
                    intent.putExtra(Intent.EXTRA_TEXT, String.format("!%s ", groupName));
                    intent.putExtra(EXTRA_ACCOUNT_KEY, accountKey);
                    startActivity(intent);
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Loader<List<ParcelableStatus>> onCreateStatusesLoader(final Context context,
                                                                    final Bundle args,
                                                                    final boolean fromUser) {
        setRefreshing(true);
        if (args == null) return null;
        final UserKey accountKey = Utils.getAccountKey(context, args);
        final String groupId = args.getString(EXTRA_GROUP_ID);
        final String groupName = args.getString(EXTRA_GROUP_NAME);
        final String maxId = args.getString(EXTRA_MAX_ID);
        final String sinceId = args.getString(EXTRA_SINCE_ID);
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        final boolean loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false);
        return new GroupTimelineLoader(getActivity(), accountKey, groupId, groupName, sinceId,
                maxId, getAdapterData(), getSavedStatusesFileArgs(), tabPosition, fromUser, loadingMore);
    }

    @Override
    protected String[] getSavedStatusesFileArgs() {
        final Bundle args = getArguments();
        assert args != null;
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final String groupId = args.getString(EXTRA_GROUP_ID);
        final String groupName = args.getString(EXTRA_GROUP_NAME);
        return new String[]{AUTHORITY_GROUP_TIMELINE, "account" + accountKey, "group_id" + groupId,
                "group_name" + groupName};
    }

    @Override
    protected String getReadPositionTagWithArguments() {
        final Bundle args = getArguments();
        assert args != null;
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        StringBuilder sb = new StringBuilder("group_");
        if (tabPosition < 0) return null;
        final String groupId = args.getString(EXTRA_GROUP_ID);
        final String groupName = args.getString(EXTRA_GROUP_NAME);
        if (groupId != null) {
            sb.append(groupId);
        } else if (groupName != null) {
            sb.append(groupName);
        }
        return sb.toString();
    }

    @NonNull
    @Override
    @TimelineType
    protected String getTimelineType() {
        return TimelineType.OTHER;
    }

}
