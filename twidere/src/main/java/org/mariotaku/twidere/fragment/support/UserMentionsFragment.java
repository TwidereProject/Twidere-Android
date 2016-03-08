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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.Loader;

import org.mariotaku.twidere.loader.support.UserMentionsLoader;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;

import java.util.List;

import edu.tsinghua.hotmobi.model.TimelineType;

public class UserMentionsFragment extends StatusesSearchFragment {

    @Override
    protected Loader<List<ParcelableStatus>> onCreateStatusesLoader(final Context context,
                                                                    final Bundle args,
                                                                    final boolean fromUser) {
        if (args == null) return null;
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final long maxId = args.getLong(EXTRA_MAX_ID, -1);
        final long sinceId = args.getLong(EXTRA_SINCE_ID, -1);
        final int page = args.getInt(EXTRA_PAGE, -1);
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        final boolean makeGap = args.getBoolean(EXTRA_MAKE_GAP, true);
        return new UserMentionsLoader(getActivity(), accountKey, screenName, maxId, sinceId, page,
                getAdapterData(), getSavedStatusesFileArgs(), tabPosition, fromUser, makeGap);
    }

    @Override
    protected String[] getSavedStatusesFileArgs() {
        final Bundle args = getArguments();
        if (args == null) return null;
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final String screen_name = args.getString(EXTRA_SCREEN_NAME);
        return new String[]{AUTHORITY_USER_MENTIONS, "account" + accountKey, "screen_name" + screen_name};
    }


    @NonNull
    @Override
    @TimelineType
    protected String getTimelineType() {
        return TimelineType.OTHER;
    }
}
