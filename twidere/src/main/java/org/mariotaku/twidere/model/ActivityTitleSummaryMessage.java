package org.mariotaku.twidere.model;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;

import org.mariotaku.microblog.library.twitter.model.Activity;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.util.ParcelableActivityExtensionsKt;
import org.mariotaku.twidere.model.util.ParcelableActivityUtils;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.oshkimaadziig.george.androidutils.SpanFormatter;

/**
 * Created by mariotaku on 16/1/1.
 */
public class ActivityTitleSummaryMessage {
    final int icon;
    final int color;
    @NonNull
    final CharSequence title;
    final CharSequence summary;

    ActivityTitleSummaryMessage(int icon, int color, @NonNull CharSequence title, @Nullable CharSequence summary) {
        this.icon = icon;
        this.color = color;
        this.title = title;
        this.summary = summary;
    }

    @Nullable
    public static ActivityTitleSummaryMessage get(Context context, UserColorNameManager manager, ParcelableActivity activity,
                                                  ParcelableUser[] sources, int defaultColor, boolean byFriends,
                                                  boolean shouldUseStarsForLikes,
                                                  boolean nameFirst) {
        final Resources resources = context.getResources();
        switch (activity.action) {
            case Activity.Action.FOLLOW: {
                int typeIcon = R.drawable.ic_activity_action_follow;
                int color = ContextCompat.getColor(context, R.color.highlight_follow);
                CharSequence title;
                if (byFriends) {
                    title = getTitleStringByFriends(resources, manager, R.string.activity_by_friends_follow,
                            R.string.activity_by_friends_follow_multi, sources, activity.target_users, nameFirst);
                } else {
                    title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_follow,
                            R.string.activity_about_me_follow_multi, sources, nameFirst);
                }
                return new ActivityTitleSummaryMessage(typeIcon, color, title, null);
            }
            case Activity.Action.FAVORITE: {
                int typeIcon;
                int color;
                CharSequence title;
                if (shouldUseStarsForLikes) {
                    typeIcon = R.drawable.ic_activity_action_favorite;
                    color = ContextCompat.getColor(context, R.color.highlight_favorite);
                    if (byFriends) {
                        title = getTitleStringByFriends(resources, manager, R.string.activity_by_friends_favorite,
                                R.string.activity_by_friends_favorite_multi, sources, activity.target_statuses, nameFirst);
                    } else {
                        title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_favorite,
                                R.string.activity_about_me_favorite_multi, sources, nameFirst);
                    }
                } else {
                    typeIcon = R.drawable.ic_activity_action_like;
                    color = ContextCompat.getColor(context, R.color.highlight_like);

                    if (byFriends) {
                        title = getTitleStringByFriends(resources, manager, R.string.activity_by_friends_like,
                                R.string.activity_by_friends_like_multi, sources, activity.target_statuses, nameFirst);
                    } else {
                        title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_like,
                                R.string.activity_about_me_like_multi, sources, nameFirst);
                    }
                }
                final CharSequence summary = generateTextOnlySummary(context, activity.target_statuses);
                return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
            }
            case Activity.Action.RETWEET: {
                int typeIcon = R.drawable.ic_activity_action_retweet;
                int color = ContextCompat.getColor(context, R.color.highlight_retweet);
                CharSequence title;
                if (byFriends) {
                    title = getTitleStringByFriends(resources, manager, R.string.activity_by_friends_retweet,
                            R.string.activity_by_friends_retweet_multi, sources, activity.target_statuses, nameFirst);
                } else {
                    title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_retweet,
                            R.string.activity_about_me_retweet_multi, sources, nameFirst);
                }
                final CharSequence summary = generateTextOnlySummary(context,
                        activity.target_object_statuses);
                return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
            }
            case Activity.Action.FAVORITED_RETWEET: {
                if (byFriends) return null;
                int typeIcon;
                int color;
                CharSequence title;
                if (shouldUseStarsForLikes) {
                    typeIcon = R.drawable.ic_activity_action_favorite;
                    color = ContextCompat.getColor(context, R.color.highlight_favorite);
                    title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_favorited_retweet,
                            R.string.activity_about_me_favorited_retweet_multi, sources, nameFirst);
                } else {
                    typeIcon = R.drawable.ic_activity_action_like;
                    color = ContextCompat.getColor(context, R.color.highlight_like);
                    title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_liked_retweet,
                            R.string.activity_about_me_liked_retweet_multi, sources, nameFirst);
                }
                final Spanned summary = generateStatusTextSummary(context, activity.target_statuses,
                        nameFirst);
                return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
            }
            case Activity.Action.RETWEETED_RETWEET: {
                if (byFriends) return null;
                int typeIcon = R.drawable.ic_activity_action_retweet;
                int color = ContextCompat.getColor(context, R.color.highlight_retweet);
                CharSequence title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_retweeted_retweet,
                        R.string.activity_about_me_retweeted_retweet_multi, sources, nameFirst);
                final Spanned summary = generateStatusTextSummary(context, activity.target_statuses,
                        nameFirst);
                return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
            }
            case Activity.Action.RETWEETED_MENTION: {
                if (byFriends) return null;
                int typeIcon = R.drawable.ic_activity_action_retweet;
                int color = ContextCompat.getColor(context, R.color.highlight_retweet);
                CharSequence title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_retweeted_mention,
                        R.string.activity_about_me_retweeted_mention_multi, sources, nameFirst);
                final Spanned summary = generateStatusTextSummary(context, activity.target_statuses,
                        nameFirst);
                return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
            }
            case Activity.Action.FAVORITED_MENTION: {
                if (byFriends) return null;
                int typeIcon;
                int color;
                CharSequence title;
                if (shouldUseStarsForLikes) {
                    typeIcon = R.drawable.ic_activity_action_favorite;
                    color = ContextCompat.getColor(context, R.color.highlight_favorite);
                    title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_favorited_mention,
                            R.string.activity_about_me_favorited_mention_multi, sources, nameFirst);
                } else {
                    typeIcon = R.drawable.ic_activity_action_like;
                    color = ContextCompat.getColor(context, R.color.highlight_like);
                    title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_liked_mention,
                            R.string.activity_about_me_liked_mention_multi, sources, nameFirst);
                }
                final Spanned summary = generateStatusTextSummary(context, activity.target_statuses,
                        nameFirst);
                return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
            }
            case Activity.Action.LIST_CREATED: {
                if (!byFriends) return null;
                int typeIcon = R.drawable.ic_activity_action_list_added;
                CharSequence title = getTitleStringByFriends(resources, manager, R.string.activity_by_friends_list_created,
                        R.string.activity_by_friends_list_created_multi, sources,
                        activity.target_object_user_lists, nameFirst);
                boolean firstLine = true;
                StringBuilder sb = new StringBuilder();
                for (ParcelableUserList item : activity.target_object_user_lists) {
                    if (!firstLine) {
                        sb.append("\n");
                    }
                    sb.append(item.description.replace('\n', ' '));
                    firstLine = false;
                }
                return new ActivityTitleSummaryMessage(typeIcon, defaultColor, title, sb);
            }
            case Activity.Action.LIST_MEMBER_ADDED: {
                if (byFriends) return null;
                CharSequence title;
                int icon = R.drawable.ic_activity_action_list_added;
                if ((sources.length == 1) && (activity.target_object_user_lists != null)
                        && (activity.target_object_user_lists.length == 1)) {
                    final SpannableString firstDisplayName = new SpannableString(manager.getDisplayName(
                            sources[0], nameFirst));
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
            case Activity.Action.MENTION:
            case Activity.Action.REPLY:
            case Activity.Action.QUOTE: {
                final ParcelableStatus status = ParcelableActivityExtensionsKt.getActivityStatus(activity);
                if (status == null) return null;
                final SpannableString title = new SpannableString(manager.getDisplayName(status,
                        nameFirst));
                title.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                return new ActivityTitleSummaryMessage(0, 0, title, status.text_unescaped);
            }
            case Activity.Action.JOINED_TWITTER: {
                int typeIcon = R.drawable.ic_activity_action_follow;
                int color = ContextCompat.getColor(context, R.color.highlight_follow);
                CharSequence title = getTitleStringAboutMe(resources, manager,
                        R.string.activity_joined_twitter, R.string.activity_joined_twitter_multi,
                        sources, nameFirst);
                return new ActivityTitleSummaryMessage(typeIcon, color, title, null);
            }
            case Activity.Action.MEDIA_TAGGED: {
                if (byFriends) return null;
                int typeIcon = R.drawable.ic_activity_action_media_tagged;
                int color = ContextCompat.getColor(context, R.color.highlight_tagged);
                CharSequence title;
                title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_media_tagged,
                        R.string.activity_about_me_media_tagged_multi, sources, nameFirst);
                final Spanned summary = generateStatusTextSummary(context, activity.target_statuses,
                        nameFirst);
                return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
            }
            case Activity.Action.FAVORITED_MEDIA_TAGGED: {
                if (byFriends) return null;
                int typeIcon;
                int color;
                CharSequence title;
                if (shouldUseStarsForLikes) {
                    typeIcon = R.drawable.ic_activity_action_favorite;
                    color = ContextCompat.getColor(context, R.color.highlight_favorite);
                    title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_favorited_media_tagged,
                            R.string.activity_about_me_favorited_media_tagged_multi, sources, nameFirst);
                } else {
                    typeIcon = R.drawable.ic_activity_action_like;
                    color = ContextCompat.getColor(context, R.color.highlight_like);
                    title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_liked_media_tagged,
                            R.string.activity_about_me_liked_media_tagged_multi, sources, nameFirst);
                }
                final Spanned summary = generateStatusTextSummary(context, activity.target_statuses,
                        nameFirst);
                return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
            }
            case Activity.Action.RETWEETED_MEDIA_TAGGED: {
                if (byFriends) return null;
                int typeIcon = R.drawable.ic_activity_action_retweet;
                int color = ContextCompat.getColor(context, R.color.highlight_retweet);
                CharSequence title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_retweeted_media_tagged,
                        R.string.activity_about_me_retweeted_media_tagged_multi, sources, nameFirst);
                final Spanned summary = generateStatusTextSummary(context, activity.target_statuses,
                        nameFirst);
                return new ActivityTitleSummaryMessage(typeIcon, color, title, summary);
            }
        }
        return null;
    }

    public static Spanned generateStatusTextSummary(Context context, ParcelableStatus[] statuses, boolean nameFirst) {
        if (statuses == null) return null;
        final SpannableStringBuilder summaryBuilder = new SpannableStringBuilder();
        boolean first = true;
        for (ParcelableStatus status : statuses) {
            if (!first) {
                summaryBuilder.append('\n');
            }
            final SpannableString displayName = new SpannableString(UserColorNameManager.decideDisplayName(status.user_nickname,
                    status.user_name, status.user_screen_name, nameFirst));
            displayName.setSpan(new StyleSpan(Typeface.BOLD), 0, displayName.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            summaryBuilder.append(SpanFormatter.format(context.getString(R.string.title_summary_line_format),
                    displayName, status.text_unescaped.replace('\n', ' ')));
            first = false;
        }
        return summaryBuilder;
    }

    public static CharSequence generateTextOnlySummary(Context context, ParcelableStatus[] statuses) {
        if (statuses == null) return null;
        final StringBuilder summaryBuilder = new StringBuilder();
        boolean first = true;
        for (ParcelableStatus status : statuses) {
            if (!first) {
                summaryBuilder.append('\n');
            }
            summaryBuilder.append(status.text_unescaped.replace('\n', ' '));
            first = false;
        }
        return summaryBuilder;
    }

    private static Spanned getTitleStringAboutMe(Resources resources, UserColorNameManager manager,
                                                 int stringRes, int stringResMulti,
                                                 ParcelableUser[] sources, boolean nameFirst) {
        if (sources == null || sources.length == 0) return null;
        final Configuration configuration = resources.getConfiguration();
        final SpannableString firstDisplayName = new SpannableString(manager.getDisplayName(sources[0],
                nameFirst));
        firstDisplayName.setSpan(new StyleSpan(Typeface.BOLD), 0, firstDisplayName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (sources.length == 1) {
            final String format = resources.getString(stringRes);
            return SpanFormatter.format(configuration.locale, format, firstDisplayName);
        } else if (sources.length == 2) {
            final String format = resources.getString(stringResMulti);
            final SpannableString secondDisplayName = new SpannableString(manager.getDisplayName(sources[1],
                    nameFirst));
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
                sources[0], nameFirst));
        firstSourceName.setSpan(new StyleSpan(Typeface.BOLD), 0, firstSourceName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        final String displayName;
        final Object target = targets[0];
        if (target instanceof ParcelableUser) {
            displayName = manager.getDisplayName((ParcelableUser) target, nameFirst);
        } else if (target instanceof ParcelableStatus) {
            displayName = manager.getDisplayName((ParcelableStatus) target, nameFirst);
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
                    nameFirst));
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

    @NonNull
    public CharSequence getTitle() {
        return title;
    }

    @Nullable
    public CharSequence getSummary() {
        return summary;
    }
}
