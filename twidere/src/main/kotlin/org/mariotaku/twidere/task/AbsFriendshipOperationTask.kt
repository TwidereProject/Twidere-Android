package org.mariotaku.twidere.task

import android.accounts.AccountManager
import android.content.Context
import android.widget.Toast
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.model.User
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.model.util.ParcelableUserUtils

/**
 * Created by mariotaku on 16/3/11.
 */
abstract class AbsFriendshipOperationTask(
        context: Context,
        @FriendshipTaskEvent.Action protected val action: Int
) : ExceptionHandlingAbstractTask<AbsFriendshipOperationTask.Arguments, ParcelableUser,
        MicroBlogException, Any?>(context) {

    override val exceptionClass = MicroBlogException::class.java

    override fun beforeExecute() {
        microBlogWrapper.addUpdatingRelationshipId(params.accountKey, params.userKey)
        val event = FriendshipTaskEvent(action, params.accountKey,
                params.userKey)
        event.isFinished = false
        bus.post(event)
    }

    override fun afterExecute(callback: Any?, result: ParcelableUser?, exception: MicroBlogException?) {
        microBlogWrapper.removeUpdatingRelationshipId(params.accountKey, params.userKey)
        val event = FriendshipTaskEvent(action, params.accountKey,
                params.userKey)
        event.isFinished = true
        if (result != null) {
            val user = result
            showSucceededMessage(params, user)
            event.isSucceeded = true
            event.user = user
        } else if (exception != null) {
            showErrorMessage(params, exception)
        }
        bus.post(event)
    }

    override fun onExecute(params: Arguments): ParcelableUser {
        val am = AccountManager.get(context)
        val details = AccountUtils.getAccountDetails(am, params.accountKey, true)
                ?: throw AccountNotFoundException()
        val twitter = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        val user = perform(twitter, details, params)
        val parcelableUser = ParcelableUserUtils.fromUser(user, params.accountKey, details.type)
        succeededWorker(twitter, details, params, parcelableUser)
        return parcelableUser
    }

    @Throws(MicroBlogException::class)
    protected abstract fun perform(twitter: MicroBlog,
            details: AccountDetails,
            args: Arguments): User

    protected abstract fun succeededWorker(twitter: MicroBlog, details: AccountDetails,
            args: Arguments, user: ParcelableUser)

    protected abstract fun showSucceededMessage(params: Arguments, user: ParcelableUser)

    protected open fun showErrorMessage(params: Arguments, exception: Exception) {
        Toast.makeText(context, exception.getErrorMessage(context), Toast.LENGTH_SHORT).show()
    }

    fun setup(accountKey: UserKey, userKey: UserKey) {
        params = Arguments(accountKey, userKey)
    }

    class Arguments(val accountKey: UserKey, val userKey: UserKey)

}
