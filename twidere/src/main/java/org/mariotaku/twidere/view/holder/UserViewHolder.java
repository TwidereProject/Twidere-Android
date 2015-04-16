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

package org.mariotaku.twidere.view.holder;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IContentCardAdapter;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.util.Locale;

import static org.mariotaku.twidere.util.UserColorNameUtils.getUserColor;
import static org.mariotaku.twidere.util.UserColorNameUtils.getUserNickname;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;

public class UserViewHolder extends ViewHolder {

    private final IContentCardAdapter adapter;

    public final IColorLabelView content;
    private final ImageView profileImageView, profileTypeView;
    private final TextView name, screenName, description, location, url, statusesCount, followersCount,
            friendsCount;
    private boolean account_color_enabled;

    public UserViewHolder(final IContentCardAdapter adapter, final View itemView) {
        super(itemView);
        this.adapter = adapter;
        content = (IColorLabelView) itemView.findViewById(R.id.content);
        profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
        profileTypeView = (ImageView) itemView.findViewById(R.id.profile_type);
        name = (TextView) itemView.findViewById(R.id.name);
        screenName = (TextView) itemView.findViewById(R.id.screen_name);
        description = (TextView) itemView.findViewById(R.id.description);
        location = (TextView) itemView.findViewById(R.id.location);
        url = (TextView) itemView.findViewById(R.id.url);
        statusesCount = (TextView) itemView.findViewById(R.id.statuses_count);
        followersCount = (TextView) itemView.findViewById(R.id.followers_count);
        friendsCount = (TextView) itemView.findViewById(R.id.friends_count);
    }

    public void setAccountColor(final int color) {
        content.drawEnd(account_color_enabled ? color : Color.TRANSPARENT);
    }

    public void setAccountColorEnabled(final boolean enabled) {
        account_color_enabled = enabled;
        if (!account_color_enabled) {
            content.drawEnd(Color.TRANSPARENT);
        }
    }

    public void setHighlightColor(final int color) {
        content.drawBackground(color);
    }

    public void setTextSize(final float textSize) {
        description.setTextSize(textSize);
        name.setTextSize(textSize);
        screenName.setTextSize(textSize * 0.75f);
        location.setTextSize(textSize);
        url.setTextSize(textSize);
        statusesCount.setTextSize(textSize);
        followersCount.setTextSize(textSize);
        friendsCount.setTextSize(textSize);
    }

    public void displayUser(ParcelableUser user) {

        final Context context = adapter.getContext();
        final MediaLoaderWrapper loader = adapter.getMediaLoader();


        setUserColor(getUserColor(context, user.id));

        final int userTypeRes = getUserTypeIconRes(user.is_verified, user.is_protected);
        if (userTypeRes != 0) {
            profileTypeView.setImageResource(userTypeRes);
        } else {
            profileTypeView.setImageDrawable(null);
        }
        name.setText(getUserNickname(context, user.id, user.name));
        screenName.setText("@" + user.screen_name);
        description.setVisibility(TextUtils.isEmpty(user.description_unescaped) ? View.GONE : View.VISIBLE);
        description.setText(user.description_unescaped);
        location.setVisibility(TextUtils.isEmpty(user.location) ? View.GONE : View.VISIBLE);
        location.setText(user.location);
        url.setVisibility(TextUtils.isEmpty(user.url_expanded) ? View.GONE : View.VISIBLE);
        url.setText(user.url_expanded);
        final Locale locale = Locale.getDefault();
        statusesCount.setText(getLocalizedNumber(locale, user.statuses_count));
        followersCount.setText(getLocalizedNumber(locale, user.followers_count));
        friendsCount.setText(getLocalizedNumber(locale, user.friends_count));
        if (adapter.isProfileImageEnabled()) {
            profileImageView.setVisibility(View.VISIBLE);
            loader.displayProfileImage(profileImageView, user.profile_image_url);
        } else {
            profileImageView.setVisibility(View.GONE);
            loader.cancelDisplayTask(profileImageView);
        }
    }

    public void setUserColor(final int color) {
        content.drawStart(color);
    }

}
