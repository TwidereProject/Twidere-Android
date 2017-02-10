package org.mariotaku.twidere.task

import android.content.Context
import android.widget.Toast
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 16/3/11.
 */
class AcceptFriendshipTask(context: Context) : AbsFriendshipOperationTask(context, FriendshipTaskEvent.Action.ACCEPT) {

    @Throws(MicroBlogException::class)
    override fun perform(twitter: MicroBlog, details: AccountDetails, args: AbsFriendshipOperationTask.Arguments): User {
        when (details.type) {
            AccountType.FANFOU -> {
                return twitter.acceptFanfouFriendship(args.userKey.id)
            }
        }
        return twitter.acceptFriendship(args.userKey.id)
    }

    override fun succeededWorker(twitter: MicroBlog, details: AccountDetails, args: AbsFriendshipOperationTask.Arguments, user: ParcelableUser) {
        Utils.setLastSeen(context, user.key, System.currentTimeMillis())
    }

    override fun showErrorMessage(params: AbsFriendshipOperationTask.Arguments, exception: Exception?) {
        Utils.showErrorMessage(context, R.string.action_accepting_follow_request, exception, false)
    }

    override fun showSucceededMessage(params: AbsFriendshipOperationTask.Arguments, user: ParcelableUser) {
        val nameFirst = kPreferences[nameFirstKey]
        Toast.makeText(context, context.getString(R.string.message_toast_accepted_users_follow_request,
                manager.getDisplayName(user, nameFirst)), Toast.LENGTH_SHORT).show()
    }

}
