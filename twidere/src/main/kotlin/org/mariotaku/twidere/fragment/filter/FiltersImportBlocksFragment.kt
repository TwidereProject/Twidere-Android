package org.mariotaku.twidere.fragment.filter

import android.content.Context
import android.os.Bundle
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_PAGINATION
import org.mariotaku.twidere.extension.linkHandlerTitle
import org.mariotaku.twidere.loader.users.AbsRequestUsersLoader
import org.mariotaku.twidere.loader.users.UserBlocksLoader
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 2016/12/26.
 */

class FiltersImportBlocksFragment : BaseFiltersImportFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.title_select_users)
    }

    override fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean):
            AbsRequestUsersLoader {
        val accountKey = args.getParcelable<UserKey>(IntentConstants.EXTRA_ACCOUNT_KEY)
        return UserBlocksLoader(context, accountKey, adapter.data, fromUser).apply {
            pagination = args.getParcelable(EXTRA_PAGINATION)
        }
    }
}
