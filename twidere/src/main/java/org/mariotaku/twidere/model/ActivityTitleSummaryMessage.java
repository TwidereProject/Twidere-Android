package org.mariotaku.twidere.model;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.api.twitter.model.Activity;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.oshkimaadziig.george.androidutils.SpanFormatter;

/**
 * Created by mariotaku on 16/1/1.
 */
public class ActivityTitleSummaryMessage {
    int icon;
    int color;
    CharSequence title;
    CharSequence summary;

    ActivityTitleSummaryMessage(int icon, int color, CharSequence title, CharSequence summary) {
        this.icon = icon;
        this.color = color;
        this.title = title;
        this.summary = summary;
    }

    public static ActivityTitleSummaryMessage get(Context context, UserColorNameManager manager, ParcelableActivity activity,
                                                  ParcelableUser[] sources, int defaultColor, boolean byFriends,
                                                  boolean shouldUseStarsForLikes,
                                                  boolean nameFirst) {
        final Resources resources = context.getResources();
        if (Activity.Action.FOLLOW.equals(activity.action)) {
            int typeIcon = (R.drawable.ic_activity_action_follow);
            int color = ContextCompat.getColor(context, R.color.highlight_follow);
            CharSequence title;
            if (byFriends) {
                title = getTitleStringByFriends(resources, manager, R.string.activity_by_friends_follow,
                        R.string.activity_by_friends_follow_multi, sources, activity.target_users, nameFirst);
            } else {
                title = (getTitleStringAboutMe(resources, manager, R.string.activity_about_me_follow,
                        R.string.activity_about_me_follow_multi, sources, nameFirst));
            }
            return new ActivityTitleSummaryMessage(typeIcon, color, title, null);
        } else if (Activity.Action.FAVORITE.equals(activity.action)) {
            int typeIcon;
            int color;
            CharSequence title;
            if (shouldUseStarsForLikes) {
                typeIcon = (R.drawable.ic_activity_action_favorite);
                color = ContextCompat.getColor(context, R.color.highlight_favorite);
                if (byFriends) {
                    title = getTitleStringByFriends(resources, manager, R.string.activity_by_friends_favorite,
                            R.string.activity_by_friends_favorite_multi, sources, activity.target_statuses, nameFirst);
                } else {
                    title = (getTitleStringAboutMe(resources, manager, R.string.activity_about_me_favorite,
                            R.string.activity_about_me_favorite_multi, sources, nameFirst));
                }
            } else {
                typeIcon = (R.drawable.ic_activity_action_like);
                color = ContextCompat.getColor(context, R.color.highlight_like);

                if (byFriends) {
                    title = (getTitleStringByFriends(resources, manager, R.string.activity_by_friends_like,
                            R.string.activity_by_friends_like_multi, sources, activity.target_statuses, nameFirst));
                } else {
                    title = (getTitleStringAboutMe(resources, manager, R.string.activity_about_me_like,
                            R.string.activity_about_me_like_multi, sources, nameFirst));
                }
            }
            final String summary = activity.target_statuses[0].text_unescaped;
            return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
        } else if (Activity.Action.RETWEET.equals(activity.action)) {
            int typeIcon = (R.drawable.ic_activity_action_retweet);
            int color = ContextCompat.getColor(context, R.color.highlight_retweet);
            CharSequence title;
            if (byFriends) {
                title = getTitleStringByFriends(resources, manager, R.string.activity_by_friends_retweet,
                        R.string.activity_by_friends_retweet_multi, sources, activity.target_statuses, nameFirst);
            } else {
                title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_retweet,
                        R.string.activity_about_me_retweet_multi, sources, nameFirst);
            }
            final String summary = activity.target_statuses[0].text_unescaped;
            return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
        } else if (Activity.Action.FAVORITED_RETWEET.equals(activity.action)) {
            if (byFriends) return null;
            int typeIcon;
            int color;
            CharSequence title;
            if (shouldUseStarsForLikes) {
                typeIcon = (R.drawable.ic_activity_action_favorite);
                color = ContextCompat.getColor(context, R.color.highlight_favorite);
                title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_favorited_retweet,
                        R.string.activity_about_me_favorited_retweet_multi, sources, nameFirst);
            } else {
                typeIcon = (R.drawable.ic_activity_action_like);
                color = ContextCompat.getColor(context, R.color.highlight_like);
                title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_liked_retweet,
                        R.string.activity_about_me_liked_retweet_multi, sources, nameFirst);
            }
            final String summary = activity.target_statuses[0].text_unescaped;
            return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
        } else if (Activity.Action.RETWEETED_RETWEET.equals(activity.action)) {
            if (byFriends) return null;
            int typeIcon = (R.drawable.ic_activity_action_retweet);
            int color = ContextCompat.getColor(context, R.color.highlight_retweet);
            CharSequence title = (getTitleStringAboutMe(resources, manager, R.string.activity_about_me_retweeted_retweet,
                    R.string.activity_about_me_retweeted_retweet_multi, sources, nameFirst));
            final String summary = activity.target_statuses[0].text_unescaped;
            return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
        } else if (Activity.Action.RETWEETED_MENTION.equals(activity.action)) {
            if (byFriends) return null;
            int typeIcon = (R.drawable.ic_activity_action_retweet);
            int color = ContextCompat.getColor(context, R.color.highlight_retweet);
            CharSequence title = (getTitleStringAboutMe(resources, manager, R.string.activity_about_me_retweeted_mention,
                    R.string.activity_about_me_retweeted_mention_multi, sources, nameFirst));
            final String summary = activity.target_statuses[0].text_unescaped;
            return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
        } else if (Activity.Action.FAVORITED_MENTION.equals(activity.action)) {
            if (byFriends) return null;
            int typeIcon;
            int color;
            CharSequence title;
            if (shouldUseStarsForLikes) {
                typeIcon = (R.drawable.ic_activity_action_favorite);
                color = ContextCompat.getColor(context, R.color.highlight_favorite);
                title = (getTitleStringAboutMe(resources, manager, R.string.activity_about_me_favorited_mention,
                        R.string.activity_about_me_favorited_mention_multi, sources, nameFirst));
            } else {
                typeIcon = (R.drawable.ic_activity_action_like);
                color = ContextCompat.getColor(context, R.color.highlight_like);
                title = (getTitleStringAboutMe(resources, manager, R.string.activity_about_me_liked_mention,
                        R.string.activity_about_me_liked_mention_multi, sources, nameFirst));
            }
            final String summary = activity.target_statuses[0].text_unescaped;
            return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
        } else if (Activity.Action.LIST_CREATED.equals(activity.action)) {
            if (!byFriends) return null;
            int typeIcon = (R.drawable.ic_activity_action_list_added);
            CharSequence title = (getTitleStringByFriends(resources, manager, R.string.activity_by_friends_list_created,
                    R.string.activity_by_friends_list_created_multi, sources,
                    activity.target_object_user_lists, nameFirst));
            boolean firstLine = true;
            StringBuilder sb = new StringBuilder();
            for (ParcelableUserList item : activity.target_object_user_lists) {
                if (!firstLine) {
                    sb.append("\n");
                }
                sb.append(item.description);
                firstLine = false;
            }
            return new ActivityTitleSummaryMessage(typeIcon, defaultColor, title, sb);
        } else if (Activity.Action.LIST_MEMBER_ADDED.equals(activity.action)) {
            if (byFriends) return null;
            CharSequence title;
            int icon = R.drawable.ic_activity_action_list_added;
            if (sources.length == 1 && activity.target_object_user_lists != null
                    && activity.target_object_user_lists.length == 1) {
                final SpannableString firstDisplayName = new SpannableString(manager.getDisplayName(
                        sources[0], nameFirst, false));
                final SpannableString listName = new SpannableString(activity.target_object_user_lists[0].name);
                firstDisplayName.setSpan(new StyleSpan(Typeface.BOLD), 0, firstDisplayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                listName.setSpan(new StyleSpan(Typeface.BOLD), 0, listName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                final String format = context.getString(R.string.activity_about_me_list_member_added_with_name);
                final Configuration configuration = resources.getConfiguration();
                title = SpanFormatter.format(configuration.locale, format, firstDisplayName,
                        listName);
            } else {
                title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_list_member_added,
                        R.string.activity_about_me_list_member_added_multi, sources, nameFirst);
            }
            return new ActivityTitleSummaryMessage(icon, defaultColor, title, null);
        }
        return null;
    }

    private static Spanned getTitleStringAboutMe(Resources resources, UserColorNameManager manager,
                                                 int stringRes, int stringResMulti,
                                                 ParcelableUser[] sources, boolean nameFirst) {
        if (sources == null || sources.length == 0) return null;
        final Configuration configuration = resources.getConfiguration();
        final SpannableString firstDisplayName = new SpannableString(manager.getDisplayName(sources[0],
                nameFirst, false));
        firstDisplayName.setSpan(new StyleSpan(Typeface.BOLD), 0, firstDisplayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (sources.length == 1) {
            final String format = resources.getString(stringRes);
            return SpanFormatter.format(configuration.locale, format, firstDisplayName);
        } else if (sources.length == 2) {
            final String format = resources.getString(stringResMulti);
            final SpannableString secondDisplayName = new SpannableString(manager.getDisplayName(sources[1],
                    nameFirst, false));
            secondDisplayName.setSpan(new StyleSpan(Typeface.BOLD), 0, secondDisplayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return SpanFormatter.format(configuration.locale, format, firstDisplayName,
                    secondDisplayName);
        } else {
            final int othersCount = sources.length - 1;
            final String nOthers = resources.getQuantityString(R.plurals.N_others, othersCount, othersCount);
            final String format = resources.getString(stringResMulti);
            return SpanFormatter.format(configuration.locale, format, firstDisplayName, nOthers);
        }
    }

    private static Spanned getTitleStringByFriends(Resources resources, UserColorNameManager manager,
                                                   int stringRes, int stringResMulti,
                                                   ParcelableUser[] sources, Object[] targets, boolean nameFirst) {
        if (sources == null || sources.length == 0) return null;
        final Configuration configuration = resources.getConfiguration();
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
            final String format = resources.getString(stringRes);
            return SpanFormatter.format(configuration.locale, format, firstSourceName, firstTargetName);
        } else if (sources.length == 2) {
            final String format = resources.getString(stringResMulti);
            final SpannableString secondSourceName = new SpannableString(manager.getDisplayName(sources[1],
                    nameFirst, false));
            secondSourceName.setSpan(new StyleSpan(Typeface.BOLD), 0, secondSourceName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return SpanFormatter.format(configuration.locale, format, firstSourceName,
                    secondSourceName, firstTargetName);
        } else {
            final int othersCount = sources.length - 1;
            final String nOthers = resources.getQuantityString(R.plurals.N_others, othersCount, othersCount);
            final String format = resources.getString(stringResMulti);
            return SpanFormatter.format(configuration.locale, format, firstSourceName, nOthers, firstTargetName);
        }
    }

    public int getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }

    public CharSequence getTitle() {
        return title;
    }

    public CharSequence getSummary() {
        return summary;
    }
}
