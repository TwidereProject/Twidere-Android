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

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.holder.UserViewListHolder;

import java.util.List;
import java.util.Locale;

import static org.mariotaku.twidere.util.UserColorNameUtils.getUserColor;
import static org.mariotaku.twidere.util.UserColorNameUtils.getUserNickname;
import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;

public class ParcelableUsersAdapter extends BaseArrayAdapter<ParcelableUser> implements IBaseCardAdapter {

    private final MediaLoaderWrapper mProfileImageLoader;
    private final MultiSelectManager mMultiSelectManager;
    private final Context mContext;

    private final Locale mLocale;

    public ParcelableUsersAdapter(final Context context) {
        this(context, Utils.isCompactCards(context));
    }

    public ParcelableUsersAdapter(final Context context, final boolean compactCards) {
        super(context, getItemResource(compactCards));
        mContext = context;
        mLocale = context.getResources().getConfiguration().locale;
        final TwidereApplication app = TwidereApplication.getInstance(context);
        mProfileImageLoader = app.getMediaLoaderWrapper();
        mMultiSelectManager = app.getMultiSelectManager();
        configBaseCardAdapter(context, this);
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position) != null ? getItem(position).id : -1;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        final Object tag = view.getTag();
        final UserViewListHolder holder;
        if (tag instanceof UserViewListHolder) {
            holder = (UserViewListHolder) tag;
        } else {
            holder = new UserViewListHolder(view);
//            holder.content.setOnOverflowIconClickListener(this);
            view.setTag(holder);
        }

        holder.position = position;

        final ParcelableUser user = getItem(position);

        final boolean showAccountColor = isShowAccountColor();

        holder.setAccountColorEnabled(showAccountColor);

        if (showAccountColor) {
            holder.setAccountColor(getAccountColor(mContext, user.account_id));
        }

        holder.setUserColor(getUserColor(mContext, user.id));

        holder.setTextSize(getTextSize());
        final int userTypeRes = getUserTypeIconRes(user.is_verified, user.is_protected);
        if (userTypeRes != 0) {
            holder.profile_type.setImageResource(userTypeRes);
        } else {
            holder.profile_type.setImageDrawable(null);
        }
        holder.name.setText(getUserNickname(mContext, user.id, user.name));
        holder.screen_name.setText("@" + user.screen_name);
        holder.description.setVisibility(TextUtils.isEmpty(user.description_unescaped) ? View.GONE : View.VISIBLE);
        holder.description.setText(user.description_unescaped);
        holder.location.setVisibility(TextUtils.isEmpty(user.location) ? View.GONE : View.VISIBLE);
        holder.location.setText(user.location);
        holder.url.setVisibility(TextUtils.isEmpty(user.url_expanded) ? View.GONE : View.VISIBLE);
        holder.url.setText(user.url_expanded);
        holder.statuses_count.setText(getLocalizedNumber(mLocale, user.statuses_count));
        holder.followers_count.setText(getLocalizedNumber(mLocale, user.followers_count));
        holder.friends_count.setText(getLocalizedNumber(mLocale, user.friends_count));
        holder.profile_image.setVisibility(isProfileImageDisplayed() ? View.VISIBLE : View.GONE);
        if (isProfileImageDisplayed()) {
            mProfileImageLoader.displayProfileImage(holder.profile_image, user.profile_image_url);
        }
        return view;
    }


    public void setData(final List<ParcelableUser> data) {
        setData(data, false);
    }

    public void setData(final List<ParcelableUser> data, final boolean clear_old) {
        if (clear_old) {
            clear();
        }
        if (data == null) return;
        for (final ParcelableUser user : data) {
            if (clear_old || findItemPosition(user.id) < 0) {
                add(user);
            }
        }
    }


    private static int getItemResource(final boolean compactCards) {
        return compactCards ? R.layout.card_item_user_compact : R.layout.card_item_user;
    }

}
