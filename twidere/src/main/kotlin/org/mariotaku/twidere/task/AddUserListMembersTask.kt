package org.mariotaku.twidere.task

import android.content.Context
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.SharedPreferenceConstants.KEY_NAME_FIRST
import org.mariotaku.twidere.model.ParcelableUser
import org.mariotaku.twidere.model.ParcelableUserList
import org.mariotaku.twidere.model.SingleResponse
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.event.UserListMembersChangedEvent
import org.mariotaku.twidere.model.util.ParcelableUserListUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.Utils

/**
 * Created by mariotaku on 2016/12/9.
 */
class AddUserListMembersTask(
        context: Context,
        private val accountKey: UserKey,
        private val listId: String,
        private val users: Array<out ParcelableUser>
) : BaseAbstractTask<Any?, SingleResponse<ParcelableUserList>, Any?>(context) {

    override fun doLongOperation(params: Any?): SingleResponse<ParcelableUserList> {
        try {
            val microBlog = MicroBlogAPIFactory.getInstance(context, accountKey) ?:
                    throw MicroBlogException("No account")
            val userIds = users.map(ParcelableUser::key).toTypedArray()
            val result = microBlog.addUserListMembers(listId, UserKey.getIds(userIds))
            val list = ParcelableUserListUtils.from(result, accountKey)
            return SingleResponse(list)
        } catch (e: MicroBlogException) {
            return SingleResponse(e)
        }

    }

    override fun afterExecute(callback: Any?, result: SingleResponse<ParcelableUserList>) {
        if (result.data != null) {
            val message: String
            if (users.size == 1) {
                val user = users.first()
                val nameFirst = preferences.getBoolean(KEY_NAME_FIRST)
                val displayName = userColorNameManager.getDisplayName(user.key, user.name,
                        user.screen_name, nameFirst)
                message = context.getString(R.string.added_user_to_list, displayName, result.data.name)
            } else {
                val res = context.resources
                message = res.getQuantityString(R.plurals.added_N_users_to_list, users.size, users.size,
                        result.data.name)
            }
            Utils.showOkMessage(context, message, false)
            bus.post(UserListMembersChangedEvent(UserListMembersChangedEvent.Action.ADDED, result.data,
                    users))
        } else {
            Utils.showErrorMessage(context, R.string.action_adding_member, result.exception, true)
        }
    }

}
