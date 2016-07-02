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
import android.support.v4.text.BidiFormatter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IGroupsAdapter;
import org.mariotaku.twidere.model.ParcelableGroup;
import org.mariotaku.twidere.model.util.UserKeyUtils;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.NameView;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.util.Locale;

/**
 * Created by mariotaku on 15/4/29.
 */
public class GroupViewHolder extends ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private final IGroupsAdapter<?> adapter;

    private final IColorLabelView itemContent;
    private final ImageView profileImageView;
    private final NameView nameView;
    private final TextView externalIndicator;
    private final TextView descriptionView;
    private final TextView membersCountView;
    private final TextView adminsCountView;

    private IGroupsAdapter.GroupAdapterListener groupClickListener;

    public GroupViewHolder(IGroupsAdapter<?> adapter, View itemView) {
        super(itemView);
        itemContent = (IColorLabelView) itemView.findViewById(R.id.itemContent);
        this.adapter = adapter;
        profileImageView = (ImageView) itemView.findViewById(R.id.profileImage);
        nameView = (NameView) itemView.findViewById(R.id.name);
        externalIndicator = (TextView) itemView.findViewById(R.id.externalIndicator);
        descriptionView = (TextView) itemView.findViewById(R.id.description);
        membersCountView = (TextView) itemView.findViewById(R.id.membersCount);
        adminsCountView = (TextView) itemView.findViewById(R.id.adminsCount);
    }

    public void displayGroup(ParcelableGroup group) {
        final Context context = adapter.getContext();
        final MediaLoaderWrapper loader = adapter.getMediaLoader();
        final BidiFormatter formatter = adapter.getBidiFormatter();

        nameView.setName(group.fullname);
        nameView.setScreenName("!" + group.nickname);

        nameView.updateText(formatter);
        final String groupHost = UserKeyUtils.getUserHost(group.url, group.account_key.getHost());
        if (UserKeyUtils.isSameHost(group.account_key.getHost(), groupHost)) {
            externalIndicator.setVisibility(View.GONE);
        } else {
            externalIndicator.setVisibility(View.VISIBLE);
            externalIndicator.setText(context.getString(R.string.external_group_host_format,
                    groupHost));
        }
        if (adapter.getProfileImageEnabled()) {
            profileImageView.setVisibility(View.VISIBLE);
            loader.displayProfileImage(profileImageView, group.homepage_logo);
        } else {
            profileImageView.setVisibility(View.GONE);
            loader.cancelDisplayTask(profileImageView);
        }
        descriptionView.setVisibility(TextUtils.isEmpty(group.description) ? View.GONE : View.VISIBLE);
        descriptionView.setText(formatter.unicodeWrap(group.description));
        membersCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), group.member_count));
        adminsCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), group.admin_count));
    }

    public void setOnClickListeners() {
        setGroupClickListener(adapter.getGroupAdapterListener());
    }

    @Override
    public void onClick(View v) {
        if (groupClickListener == null) return;
        switch (v.getId()) {
            case R.id.itemContent: {
                groupClickListener.onGroupClick(this, getLayoutPosition());
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (groupClickListener == null) return false;
        switch (v.getId()) {
            case R.id.itemContent: {
                return groupClickListener.onGroupLongClick(this, getLayoutPosition());
            }
        }
        return false;
    }

    public void setGroupClickListener(IGroupsAdapter.GroupAdapterListener listener) {
        groupClickListener = listener;
        ((View) itemContent).setOnClickListener(this);
        ((View) itemContent).setOnLongClickListener(this);
    }

    public void setupViewOptions() {
        setTextSize(adapter.getTextSize());
    }

    public void setTextSize(final float textSize) {
        descriptionView.setTextSize(textSize);
        externalIndicator.setTextSize(textSize);
        nameView.setPrimaryTextSize(textSize);
        nameView.setSecondaryTextSize(textSize * 0.75f);
        membersCountView.setTextSize(textSize);
        adminsCountView.setTextSize(textSize);
    }

}
