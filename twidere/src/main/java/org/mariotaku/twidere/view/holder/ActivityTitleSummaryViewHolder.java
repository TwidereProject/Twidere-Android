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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AbsActivitiesAdapter;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.model.ParcelableActivity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.view.ActionIconView;
import org.mariotaku.twidere.view.iface.IColorLabelView;
import org.oshkimaadziig.george.androidutils.SpanFormatter;

/**
 * Created by mariotaku on 15/1/3.
 */
public class ActivityTitleSummaryViewHolder extends ViewHolder implements View.OnClickListener {

    private final IColorLabelView itemContent;

    private final AbsActivitiesAdapter adapter;
    private final ActionIconView activityTypeView;
    private final TextView titleView;
    private final TextView summaryView;
    private final ViewGroup profileImagesContainer;
    private final TextView profileImageMoreNumber;
    private final ImageView[] profileImageViews;
    private ActivityClickListener activityClickListener;

    public ActivityTitleSummaryViewHolder(AbsActivitiesAdapter adapter, View itemView) {
        super(itemView);
        this.adapter = adapter;

        itemContent = (IColorLabelView) itemView.findViewById(R.id.item_content);

        activityTypeView = (ActionIconView) itemView.findViewById(R.id.activity_type);
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

    public void displayActivity(ParcelableActivity activity, boolean byFriends) {
        final Context context = adapter.getContext();
        final Resources resources = adapter.getContext().getResources();
        switch (activity.action) {
            case Activity.ACTION_FOLLOW: {
                activityTypeView.setImageResource(R.drawable.ic_activity_action_follow);
                activityTypeView.setColorFilter(ContextCompat.getColor(context, R.color.highlight_follow), Mode.SRC_ATOP);
                if (byFriends) {
                    titleView.setText(getTitleStringByFriends(R.string.activity_by_friends_follow,
                            R.string.activity_by_friends_follow_multi, activity.sources, activity.target_users));
                } else {
                    titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_follow,
                            R.string.activity_about_me_follow_multi, activity.sources));
                }
                displayUserProfileImages(activity.sources);
                summaryView.setVisibility(View.GONE);
                break;
            }
            case Activity.ACTION_FAVORITE: {
                activityTypeView.setImageResource(R.drawable.ic_activity_action_favorite);
                activityTypeView.setColorFilter(ContextCompat.getColor(context, R.color.highlight_favorite), Mode.SRC_ATOP);
                if (byFriends) {
                    titleView.setText(getTitleStringByFriends(R.string.activity_by_friends_favorite,
                            R.string.activity_by_friends_favorite_multi, activity.sources, activity.target_statuses));
                } else {
                    titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_favorite,
                            R.string.activity_about_me_favorite_multi, activity.sources));
                }
                displayUserProfileImages(activity.sources);
                summaryView.setText(activity.target_statuses[0].text_unescaped);
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
            case Activity.ACTION_RETWEET: {
                activityTypeView.setImageResource(R.drawable.ic_activity_action_retweet);
                activityTypeView.setColorFilter(ContextCompat.getColor(context, R.color.highlight_retweet), Mode.SRC_ATOP);
                if (byFriends) {
                    titleView.setText(getTitleStringByFriends(R.string.activity_by_friends_retweet,
                            R.string.activity_by_friends_retweet_multi, activity.sources, activity.target_statuses));
                } else
                    titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_retweet,
                            R.string.activity_about_me_retweet_multi, activity.sources));
                displayUserProfileImages(activity.sources);
                summaryView.setText(activity.target_statuses[0].text_unescaped);
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
            case Activity.ACTION_FAVORITED_RETWEET: {
                if (byFriends) {
                    showNotSupported();
                    return;
                }
                activityTypeView.setImageResource(R.drawable.ic_activity_action_favorite);
                activityTypeView.setColorFilter(ContextCompat.getColor(context, R.color.highlight_favorite), Mode.SRC_ATOP);
                titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_favorited_retweet,
                        R.string.activity_about_me_favorited_retweet_multi, activity.sources));
                displayUserProfileImages(activity.sources);
                summaryView.setText(activity.target_statuses[0].text_unescaped);
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
            case Activity.ACTION_RETWEETED_RETWEET: {
                if (byFriends) {
                    showNotSupported();
                    return;
                }
                activityTypeView.setImageResource(R.drawable.ic_activity_action_retweet);
                activityTypeView.setColorFilter(ContextCompat.getColor(context, R.color.highlight_retweet), Mode.SRC_ATOP);
                titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_retweeted_retweet,
                        R.string.activity_about_me_retweeted_retweet_multi, activity.sources));
                displayUserProfileImages(activity.sources);
                summaryView.setText(activity.target_statuses[0].text_unescaped);
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
            case Activity.ACTION_RETWEETED_MENTION: {
                if (byFriends) {
                    showNotSupported();
                    return;
                }
                activityTypeView.setImageResource(R.drawable.ic_activity_action_retweet);
                activityTypeView.setColorFilter(ContextCompat.getColor(context, R.color.highlight_retweet), Mode.SRC_ATOP);
                titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_retweeted_mention,
                        R.string.activity_about_me_retweeted_mention_multi, activity.sources));
                displayUserProfileImages(activity.sources);
                summaryView.setText(activity.target_statuses[0].text_unescaped);
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
            case Activity.ACTION_FAVORITED_MENTION: {
                if (byFriends) {
                    showNotSupported();
                    return;
                }
                activityTypeView.setImageResource(R.drawable.ic_activity_action_favorite);
                activityTypeView.setColorFilter(ContextCompat.getColor(context, R.color.highlight_favorite), Mode.SRC_ATOP);
                titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_favorited_mention,
                        R.string.activity_about_me_favorited_mention_multi, activity.sources));
                displayUserProfileImages(activity.sources);
                summaryView.setText(activity.target_statuses[0].text_unescaped);
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
            case Activity.ACTION_LIST_CREATED: {
                if (!byFriends) {
                    showNotSupported();
                    return;
                }
                activityTypeView.setImageResource(R.drawable.ic_activity_action_list_added);
                activityTypeView.setColorFilter(activityTypeView.getDefaultColor(), Mode.SRC_ATOP);
                titleView.setText(getTitleStringByFriends(R.string.activity_by_friends_list_created,
                        R.string.activity_by_friends_list_created_multi, activity.sources,
                        activity.target_object_user_lists));
                displayUserProfileImages(activity.sources);
                boolean firstLine = true;
                summaryView.setText("");
                for (ParcelableUserList item : activity.target_object_user_lists) {
                    if (!firstLine) {
                        summaryView.append("\n");
                    }
                    summaryView.append(item.description);
                    firstLine = false;
                }
                summaryView.setVisibility(View.VISIBLE);
                break;
            }
            case Activity.ACTION_LIST_MEMBER_ADDED: {
                if (byFriends) {
                    showNotSupported();
                    return;
                }
                activityTypeView.setImageResource(R.drawable.ic_activity_action_list_added);
                activityTypeView.setColorFilter(activityTypeView.getDefaultColor(), Mode.SRC_ATOP);
                if (activity.sources.length == 1 && activity.target_object_user_lists != null
                        && activity.target_object_user_lists.length == 1) {
                    final UserColorNameManager manager = adapter.getUserColorNameManager();
                    final SpannableString firstDisplayName = new SpannableString(manager.getDisplayName(
                            activity.sources[0], adapter.isNameFirst(), false));
                    final SpannableString listName = new SpannableString(activity.target_object_user_lists[0].name);
                    firstDisplayName.setSpan(new StyleSpan(Typeface.BOLD), 0, firstDisplayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    listName.setSpan(new StyleSpan(Typeface.BOLD), 0, listName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    final String format = context.getString(R.string.activity_about_me_list_member_added_with_name);
                    final Configuration configuration = resources.getConfiguration();
                    titleView.setText(SpanFormatter.format(configuration.locale, format, firstDisplayName,
                            listName));
                } else {
                    titleView.setText(getTitleStringAboutMe(R.string.activity_about_me_list_member_added,
                            R.string.activity_about_me_list_member_added_multi, activity.sources));
                }
                displayUserProfileImages(activity.sources);
                summaryView.setVisibility(View.GONE);
                break;
            }
        }
    }

    private void showNotSupported() {

    }

    public void setTextSize(float textSize) {
        titleView.setTextSize(textSize);
        summaryView.setTextSize(textSize * 0.85f);
    }

    private void displayUserProfileImages(final ParcelableUser[] statuses) {
        final MediaLoaderWrapper imageLoader = adapter.getMediaLoader();
        if (statuses == null) {
            for (final ImageView view : profileImageViews) {
                imageLoader.cancelDisplayTask(view);
                view.setVisibility(View.GONE);
            }
            return;
        }
        final int length = Math.min(profileImageViews.length, statuses.length);
        final boolean shouldDisplayImages = adapter.isProfileImageEnabled();
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

    private Spanned getTitleStringAboutMe(int stringRes, int stringResMulti, ParcelableUser[] sources) {
        if (sources == null || sources.length == 0) return null;
        final Context context = adapter.getContext();
        final boolean nameFirst = adapter.isNameFirst();
        final UserColorNameManager manager = adapter.getUserColorNameManager();
        final Resources resources = context.getResources();
        final Configuration configuration = resources.getConfiguration();
        final SpannableString firstDisplayName = new SpannableString(manager.getDisplayName(sources[0],
                nameFirst, false));
        firstDisplayName.setSpan(new StyleSpan(Typeface.BOLD), 0, firstDisplayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (sources.length == 1) {
            final String format = context.getString(stringRes);
            return SpanFormatter.format(configuration.locale, format, firstDisplayName);
        } else if (sources.length == 2) {
            final String format = context.getString(stringResMulti);
            final SpannableString secondDisplayName = new SpannableString(manager.getDisplayName(sources[1],
                    nameFirst, false));
            secondDisplayName.setSpan(new StyleSpan(Typeface.BOLD), 0, secondDisplayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return SpanFormatter.format(configuration.locale, format, firstDisplayName,
                    secondDisplayName);
        } else {
            final int othersCount = sources.length - 1;
            final SpannableString nOthers = new SpannableString(resources.getQuantityString(R.plurals.N_others, othersCount, othersCount));
            nOthers.setSpan(new StyleSpan(Typeface.BOLD), 0, nOthers.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            final String format = context.getString(stringResMulti);
            return SpanFormatter.format(configuration.locale, format, firstDisplayName, nOthers);
        }
    }

    private Spanned getTitleStringByFriends(int stringRes, int stringResMulti, ParcelableUser[] sources, Object[] targets) {
        if (sources == null || sources.length == 0) return null;
        final Context context = adapter.getContext();
        final Resources resources = context.getResources();
        final Configuration configuration = resources.getConfiguration();
        final UserColorNameManager manager = adapter.getUserColorNameManager();
        final boolean nameFirst = adapter.isNameFirst();
        final SpannableString firstSourceName = new SpannableString(manager.getDisplayName(
                sources[0], nameFirst, false));
        firstSourceName.setSpan(new StyleSpan(Typeface.BOLD), 0, firstSourceName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        final String displayName;
        final Object target = targets[0];
        if (target instanceof ParcelableUser) {
            displayName = manager.getDisplayName((ParcelableUser) target, nameFirst, false);
        } else if (target instanceof ParcelableStatus) {
            displayName = manager.getDisplayName((ParcelableStatus) target, nameFirst, false);
        } else {
            throw new IllegalArgumentException();
        }
        final SpannableString firstTargetName = new SpannableString(displayName);
        firstTargetName.setSpan(new StyleSpan(Typeface.BOLD), 0, firstTargetName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (sources.length == 1) {
            final String format = context.getString(stringRes);
            return SpanFormatter.format(configuration.locale, format, firstSourceName, firstTargetName);
        } else if (sources.length == 2) {
            final String format = context.getString(stringResMulti);
            final SpannableString secondSourceName = new SpannableString(manager.getDisplayName(sources[1],
                    nameFirst, false));
            secondSourceName.setSpan(new StyleSpan(Typeface.BOLD), 0, secondSourceName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return SpanFormatter.format(configuration.locale, format, firstSourceName,
                    secondSourceName, firstTargetName);
        } else {
            final int othersCount = sources.length - 1;
            final SpannableString nOthers = new SpannableString(resources.getQuantityString(R.plurals.N_others, othersCount, othersCount));
            nOthers.setSpan(new StyleSpan(Typeface.BOLD), 0, nOthers.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            final String format = context.getString(stringResMulti);
            return SpanFormatter.format(configuration.locale, format, firstSourceName, nOthers, firstTargetName);
        }
    }

    public void setOnClickListeners() {
        setActivityClickListener(adapter);
    }

    public void setActivityClickListener(ActivityClickListener listener) {
        activityClickListener = listener;
        ((View) itemContent).setOnClickListener(this);
//        ((View) itemContent).setOnLongClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (activityClickListener == null) return;
        final int position = getLayoutPosition();
        switch (v.getId()) {
            case R.id.item_content: {
                activityClickListener.onActivityClick(this, position);
                break;
            }
        }
    }

    public interface ActivityClickListener {

        void onActivityClick(ActivityTitleSummaryViewHolder holder, int position);
    }
}
