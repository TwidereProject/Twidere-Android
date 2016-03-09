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
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IGroupsAdapter;
import org.mariotaku.twidere.model.ParcelableGroup;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.util.Locale;

/**
 * Created by mariotaku on 15/4/29.
 */
public class GroupViewHolder extends ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    private final IGroupsAdapter<?> adapter;

    private final IColorLabelView itemContent;
    private final ImageView profileImageView;
    private final TextView nameView;
    private final TextView createdByView;
    private final TextView descriptionView;
    private final TextView membersCountView;
    private final TextView subscribersCountView;

    private IGroupsAdapter.GroupAdapterListener groupClickListener;

    public GroupViewHolder(IGroupsAdapter<?> adapter, View itemView) {
        super(itemView);
        itemContent = (IColorLabelView) itemView.findViewById(R.id.item_content);
        this.adapter = adapter;
        profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
        nameView = (TextView) itemView.findViewById(R.id.name);
        createdByView = (TextView) itemView.findViewById(R.id.created_by);
        descriptionView = (TextView) itemView.findViewById(R.id.description);
        membersCountView = (TextView) itemView.findViewById(R.id.members_count);
        subscribersCountView = (TextView) itemView.findViewById(R.id.subscribers_count);
    }

    public void displayGroup(ParcelableGroup group) {

        final Context context = adapter.getContext();
        final MediaLoaderWrapper loader = adapter.getMediaLoader();
        final UserColorNameManager manager = adapter.getUserColorNameManager();

        nameView.setText(group.fullname);
        final boolean nameFirst = adapter.isNameFirst();

        if (adapter.isProfileImageEnabled()) {
            profileImageView.setVisibility(View.VISIBLE);
            loader.displayProfileImage(profileImageView, group.homepage_logo);
        } else {
            profileImageView.setVisibility(View.GONE);
            loader.cancelDisplayTask(profileImageView);
        }
        descriptionView.setVisibility(TextUtils.isEmpty(group.description) ? View.GONE : View.VISIBLE);
        descriptionView.setText(group.description);
        membersCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), group.member_count));
        subscribersCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), group.admin_count));
    }

    public void setOnClickListeners() {
        setGroupClickListener(adapter.getGroupAdapterListener());
    }

    @Override
    public void onClick(View v) {
        if (groupClickListener == null) return;
        switch (v.getId()) {
            case R.id.item_content: {
                groupClickListener.onGroupClick(this, getLayoutPosition());
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (groupClickListener == null) return false;
        switch (v.getId()) {
            case R.id.item_content: {
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
        nameView.setTextSize(textSize);
        createdByView.setTextSize(textSize * 0.75f);
    }

}
