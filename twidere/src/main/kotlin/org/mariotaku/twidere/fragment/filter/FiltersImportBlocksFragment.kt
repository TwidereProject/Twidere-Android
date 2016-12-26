package org.mariotaku.twidere.fragment.filter

import android.content.Context
import android.os.Bundle
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.loader.CursorSupportUsersLoader
import org.mariotaku.twidere.loader.UserBlocksLoader
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 2016/12/26.
 */

class FiltersImportBlocksFragment : BaseFiltersImportFragment() {

    override fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean):
            CursorSupportUsersLoader {
        val accountKey = args.getParcelable<UserKey>(IntentConstants.EXTRA_ACCOUNT_KEY)
        val loader = UserBlocksLoader(context, accountKey, adapter.data, fromUser)
        loader.cursor = nextCursor
        loader.page = nextPage
        return loader
    }
}
