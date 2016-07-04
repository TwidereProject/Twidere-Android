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

import org.mariotaku.twidere.loader.UserFavoritesLoader;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.util.Utils;

import java.util.ArrayList;
import java.util.List;

import edu.tsinghua.hotmobi.model.TimelineType;

/**
 * Created by mariotaku on 14/12/2.
 */
public class UserFavoritesFragment extends ParcelableStatusesFragment {

    @Override
    protected Loader<List<ParcelableStatus>> onCreateStatusesLoader(final Context context,
                                                                    final Bundle args,
                                                                    final boolean fromUser) {
        setRefreshing(true);
        final UserKey accountKey = Utils.getAccountKey(context, args);
        final String maxId = args.getString(EXTRA_MAX_ID);
        final String sinceId = args.getString(EXTRA_SINCE_ID);
        final int page = args.getInt(EXTRA_PAGE, -1);
        final UserKey userKey = args.getParcelable(EXTRA_USER_KEY);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        final boolean loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false);
        return new UserFavoritesLoader(context, accountKey, userKey, screenName, sinceId, maxId,
                page,getAdapterData(), getSavedStatusesFileArgs(), tabPosition, fromUser,
                loadingMore);
    }

    @Override
    protected String[] getSavedStatusesFileArgs() {
        final Bundle args = getArguments();
        assert args != null;
        final UserKey accountKey = Utils.getAccountKey(getContext(), args);
        final UserKey userKey = args.getParcelable(EXTRA_USER_KEY);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final List<String> result = new ArrayList<>();
        result.add(AUTHORITY_USER_FAVORITES);
        result.add("account=" + accountKey);
        if (userKey != null) {
            result.add("user_id=" + userKey);
        } else if (screenName != null) {
            result.add("screen_name=" + screenName);
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
        StringBuilder sb = new StringBuilder("user_favorites_");
        if (tabPosition < 0) return null;

        final UserKey userKey = args.getParcelable(EXTRA_USER_KEY);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        if (userKey != null) {
            sb.append(userKey);
        } else if (screenName != null) {
            sb.append(screenName);
        } else {
            return null;
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
