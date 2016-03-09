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
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AbsActivitiesAdapter;
import org.mariotaku.twidere.adapter.iface.IActivitiesAdapter;
import org.mariotaku.twidere.model.ActivityTitleSummaryMessage;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.util.ParcelableActivityUtils;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.view.ActionIconView;
import org.mariotaku.twidere.view.BadgeView;
import org.mariotaku.twidere.view.ShortTimeView;
import org.mariotaku.twidere.view.iface.IColorLabelView;

/**
 * Created by mariotaku on 15/1/3.
 */
public class ActivityTitleSummaryViewHolder extends ViewHolder implements View.OnClickListener {

    private final IColorLabelView itemContent;

    private final AbsActivitiesAdapter adapter;
    private final ActionIconView activityTypeView;
    private final TextView titleView;
    private final TextView summaryView;
    private final ShortTimeView timeView;
    private final ViewGroup profileImagesContainer;
    private final BadgeView profileImageMoreNumber;
    private final ImageView[] profileImageViews;
    private final View profileImageSpace;

    private IActivitiesAdapter.ActivityAdapterListener mActivityAdapterListener;

    public ActivityTitleSummaryViewHolder(AbsActivitiesAdapter adapter, View itemView, boolean isCompact) {
        super(itemView);
        this.adapter = adapter;

        itemContent = (IColorLabelView) itemView.findViewById(R.id.item_content);
        activityTypeView = (ActionIconView) itemView.findViewById(R.id.activity_type);
        titleView = (TextView) itemView.findViewById(R.id.title);
        summaryView = (TextView) itemView.findViewById(R.id.summary);
        timeView = (ShortTimeView) itemView.findViewById(R.id.time);
        profileImageSpace = itemView.findViewById(R.id.profile_image_space);

        profileImagesContainer = (ViewGroup) itemView.findViewById(R.id.profile_images_container);
        profileImageViews = new ImageView[5];
        profileImageViews[0] = (ImageView) itemView.findViewById(R.id.activity_profile_image_0);
        profileImageViews[1] = (ImageView) itemView.findViewById(R.id.activity_profile_image_1);
        profileImageViews[2] = (ImageView) itemView.findViewById(R.id.activity_profile_image_2);
        profileImageViews[3] = (ImageView) itemView.findViewById(R.id.activity_profile_image_3);
        profileImageViews[4] = (ImageView) itemView.findViewById(R.id.activity_profile_image_4);
        profileImageMoreNumber = (BadgeView) itemView.findViewById(R.id.activity_profile_image_more_number);

        if (isCompact) {
            final Resources resources = adapter.getContext().getResources();
            final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) titleView.getLayoutParams();
            final int spacing = resources.getDimensionPixelSize(R.dimen.element_spacing_small);
            lp.leftMargin = spacing;
            MarginLayoutParamsCompat.setMarginStart(lp, spacing);
        }
        timeView.setShowAbsoluteTime(adapter.isShowAbsoluteTime());
    }

    public void displayActivity(ParcelableActivity activity, boolean byFriends) {
        final Context context = adapter.getContext();
        final ParcelableUser[] sources = ParcelableActivityUtils.getAfterFilteredSources(activity);
        final ActivityTitleSummaryMessage message = ActivityTitleSummaryMessage.get(context,
                adapter.getUserColorNameManager(), activity, sources, activityTypeView.getDefaultColor(),
                byFriends, adapter.shouldUseStarsForLikes(), adapter.isNameFirst());
        if (message == null) {
            showNotSupported();
            return;
        }
        activityTypeView.setColorFilter(message.getColor(), PorterDuff.Mode.SRC_ATOP);
        activityTypeView.setImageResource(message.getIcon());
        titleView.setText(message.getTitle());
        summaryView.setText(message.getSummary());
        summaryView.setVisibility(summaryView.length() > 0 ? View.VISIBLE : View.GONE);
        timeView.setTime(activity.timestamp);
        displayUserProfileImages(sources);
    }

    private void showNotSupported() {

    }

    public void setTextSize(float textSize) {
        titleView.setTextSize(textSize);
        summaryView.setTextSize(textSize * 0.85f);
        timeView.setTextSize(textSize * 0.80f);
    }

    private void displayUserProfileImages(final ParcelableUser[] statuses) {
        final boolean shouldDisplayImages = adapter.isProfileImageEnabled();
        profileImagesContainer.setVisibility(shouldDisplayImages ? View.VISIBLE : View.GONE);
        profileImageSpace.setVisibility(shouldDisplayImages ? View.VISIBLE : View.GONE);
        if (!shouldDisplayImages) return;
        final MediaLoaderWrapper imageLoader = adapter.getMediaLoader();
        if (statuses == null) {
            for (final ImageView view : profileImageViews) {
                imageLoader.cancelDisplayTask(view);
                view.setVisibility(View.GONE);
            }
            return;
        }
        final int length = Math.min(profileImageViews.length, statuses.length);
        for (int i = 0, j = profileImageViews.length; i < j; i++) {
            final ImageView view = profileImageViews[i];
            view.setImageDrawable(null);
            if (i < length) {
                view.setVisibility(View.VISIBLE);
                imageLoader.displayProfileImage(view, statuses[i].profile_image_url);
            } else {
                imageLoader.cancelDisplayTask(view);
                view.setVisibility(View.GONE);
            }
        }
        if (statuses.length > profileImageViews.length) {
            final int moreNumber = statuses.length - profileImageViews.length;
            profileImageMoreNumber.setVisibility(View.VISIBLE);
            profileImageMoreNumber.setText(String.valueOf(moreNumber));
        } else {
            profileImageMoreNumber.setVisibility(View.GONE);
        }
    }

    public void setOnClickListeners() {
        setActivityAdapterListener(adapter.getActivityClickListener());
    }

    public void setActivityAdapterListener(IActivitiesAdapter.ActivityAdapterListener listener) {
        mActivityAdapterListener = listener;
        ((View) itemContent).setOnClickListener(this);
//        ((View) itemContent).setOnLongClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (mActivityAdapterListener == null) return;
        final int position = getLayoutPosition();
        switch (v.getId()) {
            case R.id.item_content: {
                mActivityAdapterListener.onActivityClick(this, position);
                break;
            }
        }
    }

}
