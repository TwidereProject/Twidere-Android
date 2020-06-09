package org.mariotaku.twidere.task

import android.content.Context
import android.widget.Toast
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.nameFirstKey
import org.mariotaku.twidere.extension.model.api.microblog.toParcelable
import org.mariotaku.twidere.extension.model.newMicroBlogInstance
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.UserListMembersChangedEvent

/**
 * Created by mariotaku on 2016/12/9.
 */
class AddUserListMembersTask(
        context: Context,
        accountKey: UserKey,
        private val listId: String,
        private val users: Array<out ParcelableUser>
) : AbsAccountRequestTask<Any?, ParcelableUserList, Any?>(context, accountKey) {

    override fun onExecute(account: AccountDetails, params: Any?): ParcelableUserList {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        val userIds = users.mapToArray(ParcelableUser::key)
        val result = microBlog.addUserListMembers(listId, UserKey.getIds(userIds))
        return result.toParcelable(account.key)
    }

    override fun onSucceed(callback: Any?, result: ParcelableUserList) {
        val message: String
        message = if (users.size == 1) {
            val user = users.first()
            val nameFirst = preferences[nameFirstKey]
            val displayName = userColorNameManager.getDisplayName(user.key, user.name,
                user.screen_name, nameFirst)
            context.getString(R.string.message_toast_added_user_to_list, displayName, result.name)
        } else {
            val res = context.resources
            res.getQuantityString(R.plurals.added_N_users_to_list, users.size, users.size,
                result.name)
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        bus.post(UserListMembersChangedEvent(UserListMembersChangedEvent.Action.ADDED, result, users))
    }

}
