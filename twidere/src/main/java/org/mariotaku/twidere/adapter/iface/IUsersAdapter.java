/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.twidere.adapter.iface;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.view.holder.UserViewHolder;

/**
 * Created by mariotaku on 15/4/16.
 */
public interface IUsersAdapter<Data> extends IContentCardAdapter {

    ParcelableUser getUser(int position);

    @Nullable
    String getUserId(int position);

    int getUserCount();

    void setData(Data data);

    @Nullable
    UserAdapterListener getUserAdapterListener();

    RequestClickListener getRequestClickListener();

    boolean shouldShowAccountsColor();

    @NonNull
    @Override
    MediaLoaderWrapper getMediaLoader();

    interface UserAdapterListener {

        void onUserClick(UserViewHolder holder, int position);

        boolean onUserLongClick(UserViewHolder holder, int position);

    }

    interface RequestClickListener {

        void onAcceptClicked(UserViewHolder holder, int position);

        void onDenyClicked(UserViewHolder holder, int position);
    }
}
