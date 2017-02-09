package org.mariotaku.twidere.task

import android.content.ContentValues
import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 16/3/11.
 */
class DestroyUserMuteTask(context: Context) : AbsFriendshipOperationTask(context, FriendshipTaskEvent.Action.UNMUTE) {

    @Throws(MicroBlogException::class)
    override fun perform(twitter: MicroBlog, details: AccountDetails,
                         args: AbsFriendshipOperationTask.Arguments): User {
        return twitter.destroyMute(args.userKey.id)
    }

    override fun succeededWorker(twitter: MicroBlog,
                                 details: AccountDetails,
                                 args: AbsFriendshipOperationTask.Arguments, user: ParcelableUser) {
        val resolver = context.contentResolver
        // I bet you don't want to see this user in your auto complete list.
        val values = ContentValues()
        values.put(CachedRelationships.ACCOUNT_KEY, args.accountKey.toString())
        values.put(CachedRelationships.USER_KEY, args.userKey.toString())
        values.put(CachedRelationships.MUTING, false)
        resolver.insert(CachedRelationships.CONTENT_URI, values)
    }

    override fun showSucceededMessage(params: AbsFriendshipOperationTask.Arguments, user: ParcelableUser) {
        val nameFirst = kPreferences[nameFirstKey]
        val message = context.getString(R.string.unmuted_user, manager.getDisplayName(user,
                nameFirst))
        Utils.showInfoMessage(context, message, false)

    }

    override fun showErrorMessage(params: AbsFriendshipOperationTask.Arguments, exception: Exception?) {
        Utils.showErrorMessage(context, R.string.action_unmuting, exception, true)
    }
}
