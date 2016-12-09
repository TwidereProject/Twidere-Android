package org.mariotaku.twidere.task

import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.message.FriendshipTaskEvent
import org.mariotaku.twidere.provider.TwidereDataStore.Statuses
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 16/3/11.
 */
class DestroyFriendshipTask(context: Context) : AbsFriendshipOperationTask(context, FriendshipTaskEvent.Action.UNFOLLOW) {

    @Throws(MicroBlogException::class)
    override fun perform(twitter: MicroBlog, details: AccountDetails, args: AbsFriendshipOperationTask.Arguments): User {
        when (details.type) {
            AccountType.FANFOU -> {
                return twitter.destroyFanfouFriendship(args.userKey.id)
            }
        }
        return twitter.destroyFriendship(args.userKey.id)
    }

    override fun succeededWorker(twitter: MicroBlog, details: AccountDetails, args: AbsFriendshipOperationTask.Arguments, user: ParcelableUser) {
        user.is_following = false
        Utils.setLastSeen(context, user.key, -1)
        val where = Expression.and(Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                Expression.or(Expression.equalsArgs(Statuses.USER_KEY),
                        Expression.equalsArgs(Statuses.RETWEETED_BY_USER_KEY)))
        val whereArgs = arrayOf(args.userKey.toString(), args.userKey.toString(), args.userKey.toString())
        val resolver = context.contentResolver
        resolver.delete(Statuses.CONTENT_URI, where.sql, whereArgs)
    }

    override fun showErrorMessage(params: AbsFriendshipOperationTask.Arguments, exception: Exception?) {
        Utils.showErrorMessage(context, R.string.action_unfollowing, exception, false)
    }

    override fun showSucceededMessage(params: AbsFriendshipOperationTask.Arguments, user: ParcelableUser) {
        val nameFirst = kPreferences[nameFirstKey]
        val message = context.getString(R.string.unfollowed_user,
                manager.getDisplayName(user, nameFirst))
        Utils.showInfoMessage(context, message, false)
    }

}
