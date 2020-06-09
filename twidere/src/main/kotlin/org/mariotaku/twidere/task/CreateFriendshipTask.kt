package org.mariotaku.twidere.task

import android.content.Context
import android.widget.Toast
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.twidere.Constants
import org.mariotaku.twidere.R
import org.mariotaku.twidere.annotation.AccountType
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.model.api.mastodon.toParcelable
import org.mariotaku.twidere.extension.model.api.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 16/3/11.
 */
class CreateFriendshipTask(context: Context) : AbsFriendshipOperationTask(context, FriendshipTaskEvent.Action.FOLLOW), Constants {

    @Throws(MicroBlogException::class)
    override fun perform(details: AccountDetails, args: Arguments): ParcelableUser {
        when (details.type) {
            AccountType.FANFOU -> {
                val fanfou = details.newMicroBlogInstance(context, MicroBlog::class.java)
                return fanfou.createFanfouFriendship(args.userKey.id).toParcelable(details,
                        profileImageSize = profileImageSize)
            }
            AccountType.MASTODON -> {
                val mastodon = details.newMicroBlogInstance(context, Mastodon::class.java)
                mastodon.followUser(args.userKey.id)
                return mastodon.getAccount(args.userKey.id).toParcelable(details)
            }
            else -> {
                val twitter = details.newMicroBlogInstance(context, MicroBlog::class.java)
                return twitter.createFriendship(args.userKey.id).toParcelable(details,
                        profileImageSize = profileImageSize)
            }
        }
    }

    override fun succeededWorker(details: AccountDetails, args: Arguments, user: ParcelableUser) {
        user.is_following = true
        Utils.setLastSeen(context, user.key, System.currentTimeMillis())
    }

    override fun showSucceededMessage(params: Arguments, user: ParcelableUser) {
        val nameFirst = kPreferences[nameFirstKey]
        val message: String
        message = if (user.is_protected) {
            context.getString(R.string.sent_follow_request_to_user,
                manager.getDisplayName(user, nameFirst))
        } else {
            context.getString(R.string.followed_user,
                manager.getDisplayName(user, nameFirst))
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
