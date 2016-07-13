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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;

import org.mariotaku.twidere.loader.UserListTimelineLoader;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mariotaku on 14/12/2.
 */
public class UserListTimelineFragment extends ParcelableStatusesFragment {

    @Override
    protected Loader<List<ParcelableStatus>> onCreateStatusesLoader(final Context context,
                                                                    final Bundle args,
                                                                    final boolean fromUser) {
        setRefreshing(true);
        if (args == null) return null;
        final String listId = args.getString(EXTRA_LIST_ID);
        final UserKey accountKey = Utils.getAccountKey(context, args);
        final String maxId = args.getString(EXTRA_MAX_ID);
        final String sinceId = args.getString(EXTRA_SINCE_ID);
        final UserKey userKey = args.getParcelable(EXTRA_USER_KEY);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final String listName = args.getString(EXTRA_LIST_NAME);
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        final boolean loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false);
        return new UserListTimelineLoader(getActivity(), accountKey, listId, userKey, screenName,
                listName, sinceId, maxId, getAdapterData(), getSavedStatusesFileArgs(), tabPosition,
                fromUser, loadingMore);
    }

    @Override
    protected String[] getSavedStatusesFileArgs() {
        final Bundle args = getArguments();
        assert args != null;
        final UserKey accountKey = Utils.getAccountKey(getContext(), args);
        final String listId = args.getString(EXTRA_LIST_ID);
        final UserKey userKey = args.getParcelable(EXTRA_USER_KEY);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final String listName = args.getString(EXTRA_LIST_NAME);
        final List<String> result = new ArrayList<>();
        result.add(AUTHORITY_USER_LIST_TIMELINE);
        result.add("account=" + accountKey);
        if (listId != null) {
            result.add("list_id=" + listId);
        } else if (listName != null) {
            if (userKey != null) {
                result.add("user_id=" + userKey);
            } else if (screenName != null) {
                result.add("screen_name=" + screenName);
            }
            return null;
        } else {
            return null;
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    protected String getReadPositionTagWithArguments() {
        final Bundle args = getArguments();
        assert args != null;
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        StringBuilder sb = new StringBuilder("user_list_");
        if (tabPosition < 0) return null;
        final String listId = args.getString(EXTRA_LIST_ID);
        final String listName = args.getString(EXTRA_LIST_NAME);
        if (listId != null) {
            sb.append(listId);
        } else if (listName != null) {
            final UserKey userKey = args.getParcelable(EXTRA_USER_KEY);
            final String screenName = args.getString(EXTRA_SCREEN_NAME);
            if (userKey != null) {
                sb.append(userKey);
            } else if (screenName != null) {
                sb.append(screenName);
            } else {
                return null;
            }
            sb.append('_');
            sb.append(listName);
        } else {
            return null;
        }
        return sb.toString();
    }

}
