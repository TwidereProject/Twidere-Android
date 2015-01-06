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

package org.mariotaku.twidere.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.UserColorNameUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.UserListViewListHolder;

import java.util.List;
import java.util.Locale;

import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

public class ParcelableUserListsListAdapter extends BaseArrayAdapter<ParcelableUserList> implements IBaseCardAdapter,
        OnClickListener {

    private final Context mContext;
    private final ImageLoaderWrapper mImageLoader;
    private final MultiSelectManager mMultiSelectManager;
    private final Locale mLocale;

    public ParcelableUserListsListAdapter(final Context context) {
        this(context, Utils.isCompactCards(context));
    }

    public ParcelableUserListsListAdapter(final Context context, final boolean compactCards) {
        super(context, getItemResource(compactCards));
        mContext = context;
        mLocale = context.getResources().getConfiguration().locale;
        final TwidereApplication app = TwidereApplication.getInstance(context);
        mImageLoader = app.getImageLoaderWrapper();
        mMultiSelectManager = app.getMultiSelectManager();
        configBaseCardAdapter(context, this);
    }

    public void appendData(final List<ParcelableUserList> data) {
        setData(data, false);
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position) != null ? getItem(position).id : -1;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        final Object tag = view.getTag();
        final UserListViewListHolder holder;
        if (tag instanceof UserListViewListHolder) {
            holder = (UserListViewListHolder) tag;
        } else {
            holder = new UserListViewListHolder(view);
            holder.profile_image.setOnClickListener(this);
//            holder.content.setOnOverflowIconClickListener(this);
            view.setTag(holder);
        }

        holder.position = position;

        final ParcelableUserList user_list = getItem(position);
        final String display_name = UserColorNameUtils.getDisplayName(mContext, user_list.user_id, user_list.user_name,
                user_list.user_screen_name, isDisplayNameFirst(), isNicknameOnly(), false);
        holder.setTextSize(getTextSize());
        holder.name.setText(user_list.name);
        holder.created_by.setText(mContext.getString(R.string.created_by, display_name));
        if (holder.description != null) {
            holder.description.setVisibility(TextUtils.isEmpty(user_list.description) ? View.GONE : View.VISIBLE);
            holder.description.setText(user_list.description);
        }
        if (holder.members_count != null) {
            holder.members_count.setText(getLocalizedNumber(mLocale, user_list.members_count));
        }
        if (holder.subscribers_count != null) {
            holder.subscribers_count.setText(getLocalizedNumber(mLocale, user_list.subscribers_count));
        }
        holder.profile_image.setVisibility(isDisplayProfileImage() ? View.VISIBLE : View.GONE);
        if (isDisplayProfileImage()) {
            mImageLoader.displayProfileImage(holder.profile_image, user_list.user_profile_image_url);
        } else {
            mImageLoader.cancelDisplayTask(holder.profile_image);
        }
        holder.profile_image.setTag(position);
        return view;
    }

    @Override
    public void onClick(final View view) {
        if (mMultiSelectManager.isActive()) return;
        final Object tag = view.getTag();
        final int position = tag instanceof Integer ? (Integer) tag : -1;
        if (position == -1) return;
        switch (view.getId()) {
            case R.id.profile_image: {
                if (mContext instanceof Activity) {
                    final ParcelableUserList item = getItem(position);
                    openUserProfile(mContext, item.account_id, item.user_id, item.user_screen_name,
                            null);
                }
                break;
            }
        }
    }

    public void setData(final List<ParcelableUserList> data, final boolean clear_old) {
        if (clear_old) {
            clear();
        }
        if (data == null) return;
        for (final ParcelableUserList user : data) {
            if (clear_old || findItemPosition(user.id) < 0) {
                add(user);
            }
        }
    }


    private static int getItemResource(final boolean compactCards) {
//        return compactCards ? R.layout.card_item_user_list_compact : R.layout.card_item_user_list;
        return R.layout.list_item_user_list;
    }
}
