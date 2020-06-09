package org.mariotaku.twidere.model

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.model.activityStatus
import org.mariotaku.twidere.text.style.NonBreakEllipseSpan
import org.mariotaku.twidere.util.UserColorNameManager
import org.oshkimaadziig.george.androidutils.SpanFormatter

/**
 * Created by mariotaku on 16/1/1.
 */
class ActivityTitleSummaryMessage private constructor(val icon: Int, val color: Int, val title: CharSequence, val summary: CharSequence?) {
    companion object {

        fun get(context: Context, manager: UserColorNameManager, activity: ParcelableActivity,
                sources: Array<ParcelableLiteUser>, defaultColor: Int, shouldUseStarsForLikes: Boolean,
                nameFirst: Boolean): ActivityTitleSummaryMessage? {
            val resources = context.resources
            when (activity.action) {
                Activity.Action.FOLLOW -> {
                    val typeIcon = R.drawable.ic_activity_action_follow
                    val color = ContextCompat.getColor(context, R.color.highlight_follow)
                    val title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_follow,
                            R.string.activity_about_me_follow_multi, sources, nameFirst)
                    return ActivityTitleSummaryMessage(typeIcon, color, title, null)
                }
                Activity.Action.FAVORITE -> {
                    val typeIcon: Int
                    val color: Int
                    val title: CharSequence
                    if (shouldUseStarsForLikes) {
                        typeIcon = R.drawable.ic_activity_action_favorite
                        color = ContextCompat.getColor(context, R.color.highlight_favorite)
                        title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_favorite,
                                R.string.activity_about_me_favorite_multi, sources, nameFirst)
                    } else {
                        typeIcon = R.drawable.ic_activity_action_like
                        color = ContextCompat.getColor(context, R.color.highlight_like)

                        title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_like,
                                R.string.activity_about_me_like_multi, sources, nameFirst)
                    }
                    val summary = generateTextOnlySummary(activity.summary_line)
                    return ActivityTitleSummaryMessage(typeIcon, color, title, summary)
                }
                Activity.Action.RETWEET -> {
                    val typeIcon = R.drawable.ic_activity_action_retweet
                    val color = ContextCompat.getColor(context, R.color.highlight_retweet)
                    val title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_retweet,
                            R.string.activity_about_me_retweet_multi, sources, nameFirst)
                    val summary = generateTextOnlySummary(activity.summary_line)
                    return ActivityTitleSummaryMessage(typeIcon, color, title, summary)
                }
                Activity.Action.FAVORITED_RETWEET -> {
                    val typeIcon: Int
                    val color: Int
                    val title: CharSequence
                    if (shouldUseStarsForLikes) {
                        typeIcon = R.drawable.ic_activity_action_favorite
                        color = ContextCompat.getColor(context, R.color.highlight_favorite)
                        title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_favorited_retweet,
                                R.string.activity_about_me_favorited_retweet_multi, sources, nameFirst)
                    } else {
                        typeIcon = R.drawable.ic_activity_action_like
                        color = ContextCompat.getColor(context, R.color.highlight_like)
                        title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_liked_retweet,
                                R.string.activity_about_me_liked_retweet_multi, sources, nameFirst)
                    }
                    val summary = generateStatusTextSummary(context, manager, activity.summary_line,
                            nameFirst)
                    return ActivityTitleSummaryMessage(typeIcon, color, title, summary)
                }
                Activity.Action.RETWEETED_RETWEET -> {
                    val typeIcon = R.drawable.ic_activity_action_retweet
                    val color = ContextCompat.getColor(context, R.color.highlight_retweet)
                    val title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_retweeted_retweet,
                            R.string.activity_about_me_retweeted_retweet_multi, sources, nameFirst)
                    val summary = generateStatusTextSummary(context, manager, activity.summary_line,
                            nameFirst)
                    return ActivityTitleSummaryMessage(typeIcon, color, title, summary)
                }
                Activity.Action.RETWEETED_MENTION -> {
                    val typeIcon = R.drawable.ic_activity_action_retweet
                    val color = ContextCompat.getColor(context, R.color.highlight_retweet)
                    val title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_retweeted_mention,
                            R.string.activity_about_me_retweeted_mention_multi, sources, nameFirst)
                    val summary = generateStatusTextSummary(context, manager, activity.summary_line,
                            nameFirst)
                    return ActivityTitleSummaryMessage(typeIcon, color, title, summary)
                }
                Activity.Action.FAVORITED_MENTION -> {
                    val typeIcon: Int
                    val color: Int
                    val title: CharSequence
                    if (shouldUseStarsForLikes) {
                        typeIcon = R.drawable.ic_activity_action_favorite
                        color = ContextCompat.getColor(context, R.color.highlight_favorite)
                        title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_favorited_mention,
                                R.string.activity_about_me_favorited_mention_multi, sources, nameFirst)
                    } else {
                        typeIcon = R.drawable.ic_activity_action_like
                        color = ContextCompat.getColor(context, R.color.highlight_like)
                        title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_liked_mention,
                                R.string.activity_about_me_liked_mention_multi, sources, nameFirst)
                    }
                    val summary = generateStatusTextSummary(context, manager, activity.summary_line,
                            nameFirst)
                    return ActivityTitleSummaryMessage(typeIcon, color, title, summary)
                }
                Activity.Action.LIST_MEMBER_ADDED -> {
                    val title: CharSequence
                    val icon = R.drawable.ic_activity_action_list_added
                    title = if (sources.size == 1 && activity.summary_line?.size == 1) {
                        val firstDisplayName = SpannableString(manager.getDisplayName(
                            sources[0], nameFirst))
                        val listName = SpannableString(activity.summary_line[0].content)
                        firstDisplayName.setSpan(StyleSpan(Typeface.BOLD), 0, firstDisplayName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        listName.setSpan(StyleSpan(Typeface.BOLD), 0, listName.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                        val format = context.getString(R.string.activity_about_me_list_member_added_with_name)
                        SpanFormatter.format(format, firstDisplayName, listName)
                    } else {
                        getTitleStringAboutMe(resources, manager, R.string.activity_about_me_list_member_added,
                            R.string.activity_about_me_list_member_added_multi, sources, nameFirst)
                    }
                    return ActivityTitleSummaryMessage(icon, defaultColor, title, null)
                }
                Activity.Action.MENTION, Activity.Action.REPLY, Activity.Action.QUOTE -> {
                    val status = activity.activityStatus ?: return null
                    val title = SpannableString(manager.getDisplayName(status,
                            nameFirst))
                    title.setSpan(StyleSpan(Typeface.BOLD), 0, title.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    return ActivityTitleSummaryMessage(0, 0, title, status.text_unescaped)
                }
                Activity.Action.JOINED_TWITTER -> {
                    val typeIcon = R.drawable.ic_activity_action_follow
                    val color = ContextCompat.getColor(context, R.color.highlight_follow)
                    val title = getTitleStringAboutMe(resources, manager,
                            R.string.activity_joined_twitter, R.string.activity_joined_twitter_multi,
                            sources, nameFirst)
                    return ActivityTitleSummaryMessage(typeIcon, color, title, null)
                }
                Activity.Action.MEDIA_TAGGED -> {
                    val typeIcon = R.drawable.ic_activity_action_media_tagged
                    val color = ContextCompat.getColor(context, R.color.highlight_tagged)
                    val title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_media_tagged,
                            R.string.activity_about_me_media_tagged_multi, sources, nameFirst)
                    val summary = generateStatusTextSummary(context, manager, activity.summary_line,
                            nameFirst)
                    return ActivityTitleSummaryMessage(typeIcon, color, title, summary)
                }
                Activity.Action.FAVORITED_MEDIA_TAGGED -> {
                    val typeIcon: Int
                    val color: Int
                    val title: CharSequence
                    if (shouldUseStarsForLikes) {
                        typeIcon = R.drawable.ic_activity_action_favorite
                        color = ContextCompat.getColor(context, R.color.highlight_favorite)
                        title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_favorited_media_tagged,
                                R.string.activity_about_me_favorited_media_tagged_multi, sources, nameFirst)
                    } else {
                        typeIcon = R.drawable.ic_activity_action_like
                        color = ContextCompat.getColor(context, R.color.highlight_like)
                        title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_liked_media_tagged,
                                R.string.activity_about_me_liked_media_tagged_multi, sources, nameFirst)
                    }
                    val summary = generateStatusTextSummary(context, manager, activity.summary_line,
                            nameFirst)
                    return ActivityTitleSummaryMessage(typeIcon, color, title, summary)
                }
                Activity.Action.RETWEETED_MEDIA_TAGGED -> {
                    val typeIcon = R.drawable.ic_activity_action_retweet
                    val color = ContextCompat.getColor(context, R.color.highlight_retweet)
                    val title = getTitleStringAboutMe(resources, manager, R.string.activity_about_me_retweeted_media_tagged,
                            R.string.activity_about_me_retweeted_media_tagged_multi, sources, nameFirst)
                    val summary = generateStatusTextSummary(context, manager, activity.summary_line,
                            nameFirst)
                    return ActivityTitleSummaryMessage(typeIcon, color, title, summary)
                }
            }
            return null
        }

        private fun generateStatusTextSummary(context: Context, manager: UserColorNameManager,
                statuses: Array<ParcelableActivity.SummaryLine>?, nameFirst: Boolean): Spanned? {
            return statuses?.joinTo(SpannableStringBuilder(), separator = "\n") { status ->
                val displayName = SpannableString(manager.getDisplayName(status.key,
                        status.name, status.screen_name, nameFirst)).also {
                    it.setSpan(StyleSpan(Typeface.BOLD), 0, it.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                val statusText = if (statuses.size > 1) {
                    SpannableString(status.content.replace('\n', ' ')).also {
                        it.setSpan(NonBreakEllipseSpan(), 0, it.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                } else {
                    status.content
                }
                return@joinTo SpanFormatter.format(context.getString(R.string.title_summary_line_format),
                        displayName, statusText)
            }
        }

        private fun generateTextOnlySummary(lines: Array<ParcelableActivity.SummaryLine>?): CharSequence? {
            return lines?.joinTo(SpannableStringBuilder(), separator = "\n") { status ->
                if (lines.size > 1) {
                    return@joinTo SpannableString(status.content.replace('\n', ' ')).also {
                        it.setSpan(NonBreakEllipseSpan(), 0, it.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                } else {
                    return@joinTo status.content
                }
            }
        }

        private fun getTitleStringAboutMe(resources: Resources, manager: UserColorNameManager,
                stringRes: Int, stringResMulti: Int, sources: Array<ParcelableLiteUser>,
                nameFirst: Boolean): CharSequence {
            val firstDisplayName = SpannableString(manager.getDisplayName(sources[0],
                    nameFirst))
            firstDisplayName.setSpan(StyleSpan(Typeface.BOLD), 0, firstDisplayName.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            when (sources.size) {
                1 -> {
                    val format = resources.getString(stringRes)
                    return SpanFormatter.format(format, firstDisplayName)
                }
                2 -> {
                    val format = resources.getString(stringResMulti)
                    val secondDisplayName = SpannableString(manager.getDisplayName(sources[1],
                        nameFirst))
                    secondDisplayName.setSpan(StyleSpan(Typeface.BOLD), 0, secondDisplayName.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    return SpanFormatter.format(format, firstDisplayName,
                        secondDisplayName)
                }
                else -> {
                    val othersCount = sources.size - 1
                    val nOthers = resources.getQuantityString(R.plurals.N_others, othersCount, othersCount)
                    val format = resources.getString(stringResMulti)
                    return SpanFormatter.format(format, firstDisplayName, nOthers)
                }
            }
        }
    }
}
