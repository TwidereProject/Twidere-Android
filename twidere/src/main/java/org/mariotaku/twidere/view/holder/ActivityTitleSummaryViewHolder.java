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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AbsActivitiesAdapter;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.UserColorNameUtils;

/**
 * Created by mariotaku on 15/1/3.
 */
public class ActivityTitleSummaryViewHolder extends ViewHolder {

    private final AbsActivitiesAdapter adapter;
    private final ImageView activityTypeView;
    private final TextView titleView;
    private final TextView summaryView;
    private final ViewGroup profileImagesContainer;
    private final TextView profileImageMoreNumber;
    private final ImageView[] profileImageViews;

    public ActivityTitleSummaryViewHolder(AbsActivitiesAdapter adapter, View itemView) {
        super(itemView);
        this.adapter = adapter;
        activityTypeView = (ImageView) itemView.findViewById(R.id.activity_type);
        titleView = (TextView) itemView.findViewById(R.id.title);
        summaryView = (TextView) itemView.findViewById(R.id.summary);

        profileImagesContainer = (ViewGroup) itemView.findViewById(R.id.profile_images_container);
        profileImageViews = new ImageView[5];
        profileImageViews[0] = (ImageView) itemView.findViewById(R.id.activity_profile_image_0);
        profileImageViews[1] = (ImageView) itemView.findViewById(R.id.activity_profile_image_1);
        profileImageViews[2] = (ImageView) itemView.findViewById(R.id.activity_profile_image_2);
        profileImageViews[3] = (ImageView) itemView.findViewById(R.id.activity_profile_image_3);
        profileImageViews[4] = (ImageView) itemView.findViewById(R.id.activity_profile_image_4);
        profileImageMoreNumber = (TextView) itemView.findViewById(R.id.activity_profile_image_more_number);
    }

    public void displayActivity(ParcelableActivity activity) {
        final Context context = adapter.getContext();
        switch (activity.action) {
            case ParcelableActivity.ACTION_FOLLOW: {
                activityTypeView.setImageResource(R.drawable.ic_indicator_followers);
                final String firstDisplayName = UserColorNameUtils.getDisplayName(context,
                        activity.sources[0]);
                if (activity.sources.length > 1) {
                    titleView.setText(context.getString(R.string.activity_about_me_follow_multi,
                            firstDisplayName, activity.sources.length - 1));
                } else {
                    titleView.setText(context.getString(R.string.activity_about_me_follow,
                            firstDisplayName));
                }
                displayUserProfileImages(activity.sources);
                summaryView.setVisibility(View.GONE);
                break;
            }
            case ParcelableActivity.ACTION_FAVORITE: {
                activityTypeView.setImageResource(R.drawable.ic_indicator_starred);
                final String firstDisplayName = UserColorNameUtils.getDisplayName(context,
                        activity.sources[0]);
                if (activity.sources.length > 1) {
                    titleView.setText(context.getString(R.string.activity_about_me_favorite_multi,
                            firstDisplayName, activity.sources.length - 1));
                } else {
                    titleView.setText(context.getString(R.string.activity_about_me_favorite,
                            firstDisplayName));
                }
                displayUserProfileImages(activity.sources);
                summaryView.setText(activity.target_statuses[0].text_unescaped);
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
            case ParcelableActivity.ACTION_RETWEET: {
                activityTypeView.setImageResource(R.drawable.ic_indicator_retweet);
                final String firstDisplayName = UserColorNameUtils.getDisplayName(context,
                        activity.sources[0]);
                if (activity.sources.length > 1) {
                    titleView.setText(context.getString(R.string.activity_about_me_retweet_multi,
                            firstDisplayName, activity.sources.length - 1));
                } else {
                    titleView.setText(context.getString(R.string.activity_about_me_retweet,
                            firstDisplayName));
                }
                displayUserProfileImages(activity.sources);
                summaryView.setText(activity.target_statuses[0].text_unescaped);
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    public void setTextSize(float textSize) {
        titleView.setTextSize(textSize);
        summaryView.setTextSize(textSize * 0.85f);
    }

    private void displayUserProfileImages(final ParcelableUser[] statuses) {
        final ImageLoaderWrapper imageLoader = adapter.getImageLoader();
        if (statuses == null) {
            for (final ImageView view : profileImageViews) {
                imageLoader.cancelDisplayTask(view);
                view.setVisibility(View.GONE);
            }
            return;
        }
        final int length = Math.min(profileImageViews.length, statuses.length);
        final boolean shouldDisplayImages = true;
        profileImagesContainer.setVisibility(shouldDisplayImages ? View.VISIBLE : View.GONE);
        if (!shouldDisplayImages) return;
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
            final Context context = adapter.getContext();
            final int moreNumber = statuses.length - profileImageViews.length;
            profileImageMoreNumber.setVisibility(View.VISIBLE);
            profileImageMoreNumber.setText(context.getString(R.string.and_more, moreNumber));
        } else {
            profileImageMoreNumber.setVisibility(View.GONE);
        }
    }

}
