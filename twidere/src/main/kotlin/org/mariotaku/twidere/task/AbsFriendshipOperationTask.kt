package org.mariotaku.twidere.task

import android.accounts.AccountManager
import android.content.Context
import android.widget.Toast
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.R
import org.mariotaku.twidere.exception.AccountNotFoundException
import org.mariotaku.twidere.extension.getErrorMessage
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.FriendshipTaskEvent
import org.mariotaku.twidere.model.util.AccountUtils

/**
 * Created by mariotaku on 16/3/11.
 */
abstract class AbsFriendshipOperationTask(
        context: Context,
        @FriendshipTaskEvent.Action protected val action: Int
) : ExceptionHandlingAbstractTask<AbsFriendshipOperationTask.Arguments, ParcelableUser,
        MicroBlogException, Any?>(context) {

    protected val profileImageSize: String = context.getString(R.string.profile_image_size)
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
        val event = FriendshipTaskEvent(action, params.accountKey, params.userKey)
        event.isFinished = true
        if (result != null) {
            showSucceededMessage(params, result)
            event.isSucceeded = true
            event.user = result
        } else if (exception != null) {
            showErrorMessage(params, exception)
        }
        bus.post(event)
    }

    override fun onExecute(params: Arguments): ParcelableUser {
        val am = AccountManager.get(context)
        val details = AccountUtils.getAccountDetails(am, params.accountKey, true)
                ?: throw AccountNotFoundException()
        val user = perform(details, params)
        succeededWorker(details, params, user)
        return user
    }

    fun setup(accountKey: UserKey, userKey: UserKey, screenName: String? = null) {
        params = Arguments(accountKey, userKey, screenName)
    }

    protected open fun showErrorMessage(params: Arguments, exception: Exception) {
        Toast.makeText(context, exception.getErrorMessage(context), Toast.LENGTH_SHORT).show()
    }

    @Throws(MicroBlogException::class)
    protected abstract fun perform(details: AccountDetails, args: Arguments): ParcelableUser

    protected abstract fun succeededWorker(details: AccountDetails, args: Arguments,
            user: ParcelableUser)

    protected abstract fun showSucceededMessage(params: Arguments, user: ParcelableUser)

    class Arguments(val accountKey: UserKey, val userKey: UserKey, val screenName: String?)

}
