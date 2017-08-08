package org.mariotaku.twidere.fragment.filter

import android.content.Context
import android.os.Bundle
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.loader.users.AbsRequestUsersLoader
import org.mariotaku.twidere.loader.users.MutesUsersLoader
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 2016/12/26.
 */
class FiltersImportMutesFragment : BaseFiltersImportFragment() {

    override fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean):
            AbsRequestUsersLoader {
        val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        return MutesUsersLoader(context, accountKey, adapter.data, fromUser).apply {
            pagination = args.getParcelable(IntentConstants.EXTRA_PAGINATION)
        }
    }

}