package org.mariotaku.twidere.fragment

import android.content.Context
import android.os.Bundle
import androidx.loader.content.Loader
import org.mariotaku.twidere.constant.IntentConstants.*
import org.mariotaku.twidere.loader.group.UserGroupsLoader
import org.mariotaku.twidere.model.ParcelableGroup
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 16/3/9.
 */
class UserGroupsFragment : ParcelableGroupsFragment() {
    override fun onCreateUserListsLoader(context: Context, args: Bundle, fromUser: Boolean): Loader<List<ParcelableGroup>?> {
        val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)!!
        val userKey = args.getParcelable<UserKey>(EXTRA_USER_KEY)
        val screenName = args.getString(EXTRA_SCREEN_NAME)
        return UserGroupsLoader(context, accountKey, userKey, screenName, adapter.getData())
    }

}
