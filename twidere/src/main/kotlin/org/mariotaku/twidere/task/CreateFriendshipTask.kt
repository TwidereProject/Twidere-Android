package org.mariotaku.twidere.task

import android.content.Context
import android.text.TextUtils
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.USER_TYPE_FANFOU_COM
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 16/3/11.
 */
class CreateFriendshipTask(context: Context) : AbsFriendshipOperationTask(context, FriendshipTaskEvent.Action.FOLLOW), Constants {

    @Throws(MicroBlogException::class)
    override fun perform(twitter: MicroBlog, details: AccountDetails, args: AbsFriendshipOperationTask.Arguments): User {
        when (details.type) {
            AccountType.FANFOU -> {
                return twitter.createFanfouFriendship(args.userKey.id)
            }
        }
        return twitter.createFriendship(args.userKey.id)
    }

    override fun succeededWorker(twitter: MicroBlog, details: AccountDetails, args: AbsFriendshipOperationTask.Arguments, user: ParcelableUser) {
        user.is_following = true
        Utils.setLastSeen(context, user.key, System.currentTimeMillis())
    }

    override fun showErrorMessage(params: AbsFriendshipOperationTask.Arguments, exception: Exception?) {
        if (USER_TYPE_FANFOU_COM == params.accountKey.host) {
            // Fanfou returns 403 for follow request
            if (exception is MicroBlogException) {
                if (exception.statusCode == 403 && !TextUtils.isEmpty(exception.errorMessage)) {
                    Utils.showErrorMessage(context, exception.errorMessage, false)
                    return
                }
            }
        }
        Utils.showErrorMessage(context, R.string.action_following, exception, false)
    }

    override fun showSucceededMessage(params: AbsFriendshipOperationTask.Arguments, user: ParcelableUser) {
        val nameFirst = kPreferences[nameFirstKey]
        val message: String
        if (user.is_protected) {
            message = context.getString(R.string.sent_follow_request_to_user,
                    manager.getDisplayName(user, nameFirst))
        } else {
            message = context.getString(R.string.followed_user,
                    manager.getDisplayName(user, nameFirst))
        }
        Utils.showOkMessage(context, message, false)
    }

}
