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

package org.mariotaku.twidere.loader.support;

import android.content.Context;

import org.mariotaku.twidere.api.twitter.Twitter;
import org.mariotaku.twidere.api.twitter.TwitterException;
import org.mariotaku.twidere.api.twitter.model.Paging;
import org.mariotaku.twidere.api.twitter.model.User;
import org.mariotaku.twidere.model.ParcelableUser;

import java.util.List;

public class UserSearchLoader extends TwitterAPIUsersLoader {

    private final String mQuery;
    private final int mPage;

    public UserSearchLoader(final Context context, final long accountId, final String query, final int page,
                            final List<ParcelableUser> data, boolean fromUser) {
        super(context, accountId, data, fromUser);
        mQuery = query;
        mPage = page;
    }

    public int getPage() {
        return mPage;
    }

    public String getQuery() {
        return mQuery;
    }

    @Override
    public List<User> getUsers(final Twitter twitter) throws TwitterException {
        if (twitter == null) return null;
        final Paging paging = new Paging();
        paging.page(mPage);
        return twitter.searchUsers(mQuery, paging);
    }

    protected long getUserPosition(final User user, final int index) {
        return (mPage + 1) * 20 + index;
    }
}
