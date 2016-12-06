package org.mariotaku.twidere.task

import android.content.Context
import android.net.Uri
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.Activity
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.ResponseList
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.annotation.ReadPositionTag
import org.mariotaku.twidere.extension.model.isOfficial
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.provider.TwidereDataStore.Activities
import org.mariotaku.twidere.task.twitter.GetActivitiesTask
import org.mariotaku.twidere.util.ErrorInfoStore
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 16/2/11.
 */
class GetActivitiesAboutMeTask(context: Context) : GetActivitiesTask(context) {

    override val errorInfoKey: String
        get() = ErrorInfoStore.KEY_INTERACTIONS

    override fun saveReadPosition(accountKey: UserKey, details: AccountDetails, twitter: MicroBlog) {
        if (AccountType.TWITTER == details.type && details.isOfficial(context)) {
            try {
                val response = twitter.getActivitiesAboutMeUnread(true)
                val tag = Utils.getReadPositionTagWithAccount(ReadPositionTag.ACTIVITIES_ABOUT_ME,
                        accountKey)
                readStateManager.setPosition(tag, response.cursor, false)
            } catch (e: MicroBlogException) {
                // Ignore
            }
        }
    }

    @Throws(MicroBlogException::class)
    override fun getActivities(twitter: MicroBlog,
                               details: AccountDetails,
                               paging: Paging): ResponseList<Activity> {
        if (details.isOfficial(context)) {
            return twitter.getActivitiesAboutMe(paging)
        }
        val activities = ResponseList<Activity>()
        val statuses: ResponseList<Status>
        when (details.type) {
            AccountType.FANFOU -> {
                statuses = twitter.getMentions(paging)
            }
            else -> {
                statuses = twitter.getMentionsTimeline(paging)
            }
        }
        statuses.mapTo(activities) { Activity.fromMention(details.key.id, it) }
        return activities
    }

    override val contentUri: Uri
        get() = Activities.AboutMe.CONTENT_URI
}
