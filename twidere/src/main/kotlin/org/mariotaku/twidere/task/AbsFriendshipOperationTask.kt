package org.mariotaku.twidere.task

import android.accounts.AccountManager
import android.content.Context
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.message.FriendshipTaskEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableUserUtils

/**
 * Created by mariotaku on 16/3/11.
 */
abstract class AbsFriendshipOperationTask(
        context: Context,
        @FriendshipTaskEvent.Action protected val action: Int
) : BaseAbstractTask<AbsFriendshipOperationTask.Arguments, SingleResponse<ParcelableUser>, Any?>(context) {

    override fun beforeExecute() {
        microBlogWrapper.addUpdatingRelationshipId(params.accountKey, params.userKey)
        val event = FriendshipTaskEvent(action, params.accountKey,
                params.userKey)
        event.isFinished = false
        bus.post(event)
    }

    override fun afterExecute(handler: Any?, result: SingleResponse<ParcelableUser>?) {
        microBlogWrapper.removeUpdatingRelationshipId(params.accountKey, params.userKey)
        val event = FriendshipTaskEvent(action, params.accountKey,
                params.userKey)
        event.isFinished = true
        if (result!!.hasData()) {
            val user = result.data!!
            showSucceededMessage(params, user)
            event.isSucceeded = true
            event.user = result.data
        } else {
            showErrorMessage(params, result.exception)
        }
        bus.post(event)
    }

    override fun doLongOperation(args: Arguments): SingleResponse<ParcelableUser> {
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), args.accountKey, true) ?: return SingleResponse.getInstance()
        val twitter = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        try {
            val user = perform(twitter, details, args)
            val parcelableUser = ParcelableUserUtils.fromUser(user, args.accountKey)
            succeededWorker(twitter, details, args, parcelableUser)
            return SingleResponse.getInstance(parcelableUser)
        } catch (e: MicroBlogException) {
            return SingleResponse.getInstance<ParcelableUser>(e)
        }

    }

    @Throws(MicroBlogException::class)
    protected abstract fun perform(twitter: MicroBlog,
                                   details: AccountDetails,
                                   args: Arguments): User

    protected abstract fun succeededWorker(twitter: MicroBlog,
                                           details: AccountDetails,
                                           args: Arguments,
                                           user: ParcelableUser)

    protected abstract fun showSucceededMessage(params: Arguments, user: ParcelableUser)

    protected abstract fun showErrorMessage(params: Arguments, exception: Exception?)

    fun setup(accountKey: UserKey, userKey: UserKey) {
        params = Arguments(accountKey, userKey)
    }

    class Arguments(val accountKey: UserKey, val userKey: UserKey)

}
