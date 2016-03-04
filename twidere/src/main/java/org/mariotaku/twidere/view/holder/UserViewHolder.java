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

import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IUsersAdapter;
import org.mariotaku.twidere.adapter.iface.IUsersAdapter.RequestClickListener;
import org.mariotaku.twidere.adapter.iface.IUsersAdapter.UserAdapterListener;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.view.NameView;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.util.Locale;

import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;

public class UserViewHolder extends ViewHolder implements OnClickListener, OnLongClickListener {

    private final IUsersAdapter<?> adapter;

    private final IColorLabelView itemContent;
    private final ImageView profileImageView;
    private final ImageView profileTypeView;
    private final NameView nameView;
    private final TextView descriptionView, locationView, urlView,
            statusesCountView, followersCountView, friendsCountView;

    private final View acceptRequestButton, denyRequestButton;
    private final View followRequestContainer;
    private final View processingRequestProgress;

    private UserAdapterListener userClickListener;
    private RequestClickListener requestClickListener;

    public UserViewHolder(final IUsersAdapter<?> adapter, final View itemView) {
        super(itemView);
        this.adapter = adapter;
        itemContent = (IColorLabelView) itemView.findViewById(R.id.item_content);
        profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
        profileTypeView = (ImageView) itemView.findViewById(R.id.profile_type);
        nameView = (NameView) itemView.findViewById(R.id.name);
        descriptionView = (TextView) itemView.findViewById(R.id.description);
        locationView = (TextView) itemView.findViewById(R.id.location);
        urlView = (TextView) itemView.findViewById(R.id.url);
        statusesCountView = (TextView) itemView.findViewById(R.id.statuses_count);
        followersCountView = (TextView) itemView.findViewById(R.id.followers_count);
        friendsCountView = (TextView) itemView.findViewById(R.id.friends_count);
        followRequestContainer = itemView.findViewById(R.id.actions_container);
        acceptRequestButton = itemView.findViewById(R.id.accept_request);
        denyRequestButton = itemView.findViewById(R.id.deny_request);
        processingRequestProgress = itemView.findViewById(R.id.processing_request);
    }

    public void displayUser(ParcelableUser user) {

        final MediaLoaderWrapper loader = adapter.getMediaLoader();
        final UserColorNameManager manager = adapter.getUserColorNameManager();
        final AsyncTwitterWrapper twitter = adapter.getTwitterWrapper();


        itemContent.drawStart(manager.getUserColor(user.id, false));

        final int userTypeRes = getUserTypeIconRes(user.is_verified, user.is_protected);
        if (userTypeRes != 0) {
            profileTypeView.setImageResource(userTypeRes);
        } else {
            profileTypeView.setImageDrawable(null);
        }
        nameView.setName(manager.getUserNickname(user.id, user.name, false));
        nameView.setScreenName("@" + user.screen_name);
        nameView.updateText(adapter.getBidiFormatter());
        descriptionView.setVisibility(TextUtils.isEmpty(user.description_unescaped) ? View.GONE : View.VISIBLE);
        descriptionView.setText(user.description_unescaped);
        locationView.setVisibility(TextUtils.isEmpty(user.location) ? View.GONE : View.VISIBLE);
        locationView.setText(user.location);
        urlView.setVisibility(TextUtils.isEmpty(user.url_expanded) ? View.GONE : View.VISIBLE);
        urlView.setText(user.url_expanded);
        final Locale locale = Locale.getDefault();
        statusesCountView.setText(getLocalizedNumber(locale, user.statuses_count));
        followersCountView.setText(getLocalizedNumber(locale, user.followers_count));
        friendsCountView.setText(getLocalizedNumber(locale, user.friends_count));
        if (adapter.isProfileImageEnabled()) {
            profileImageView.setVisibility(View.VISIBLE);
            loader.displayProfileImage(profileImageView, user);
        } else {
            profileImageView.setVisibility(View.GONE);
            loader.cancelDisplayTask(profileImageView);
        }

        if (twitter.isProcessingFollowRequest(user.account_id, user.id)) {
            processingRequestProgress.setVisibility(View.VISIBLE);
            acceptRequestButton.setVisibility(View.GONE);
            denyRequestButton.setVisibility(View.GONE);
        } else {
            processingRequestProgress.setVisibility(View.GONE);
            acceptRequestButton.setVisibility(View.VISIBLE);
            denyRequestButton.setVisibility(View.VISIBLE);
        }
    }

    public ImageView getProfileImageView() {
        return profileImageView;
    }

    public ImageView getProfileTypeView() {
        return profileTypeView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_content: {
                if (userClickListener == null) return;
                userClickListener.onUserClick(this, getLayoutPosition());
                break;
            }
            case R.id.accept_request: {
                if (requestClickListener == null) return;
                requestClickListener.onAcceptClicked(this, getLayoutPosition());
                break;
            }
            case R.id.deny_request: {
                if (requestClickListener == null) return;
                requestClickListener.onDenyClicked(this, getLayoutPosition());
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (userClickListener == null) return false;
        switch (v.getId()) {
            case R.id.item_content: {
                return userClickListener.onUserLongClick(this, getLayoutPosition());
            }
        }
        return false;
    }

    public void setOnClickListeners() {
        setUserClickListener(adapter.getUserAdapterListener());
        setRequestClickListener(adapter.getRequestClickListener());
    }

    private void setRequestClickListener(RequestClickListener listener) {
        requestClickListener = listener;
        if (listener != null) {
            nameView.setTwoLine(true);
            followRequestContainer.setVisibility(View.VISIBLE);
        } else {
            nameView.setTwoLine(false);
            followRequestContainer.setVisibility(View.GONE);
        }
        nameView.updateText();
        acceptRequestButton.setOnClickListener(this);
        denyRequestButton.setOnClickListener(this);
    }

    public void setTextSize(final float textSize) {
        descriptionView.setTextSize(textSize);
        nameView.setPrimaryTextSize(textSize);
        nameView.setSecondaryTextSize(textSize * 0.75f);
        locationView.setTextSize(textSize);
        urlView.setTextSize(textSize);
        statusesCountView.setTextSize(textSize);
        followersCountView.setTextSize(textSize);
        friendsCountView.setTextSize(textSize);
    }

    public void setUserClickListener(UserAdapterListener listener) {
        userClickListener = listener;
        ((View) itemContent).setOnClickListener(this);
        ((View) itemContent).setOnLongClickListener(this);
    }

    public void setupViewOptions() {
        setTextSize(adapter.getTextSize());
    }

}
