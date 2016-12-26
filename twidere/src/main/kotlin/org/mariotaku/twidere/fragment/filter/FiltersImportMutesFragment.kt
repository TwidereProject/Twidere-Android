package org.mariotaku.twidere.fragment.filter

import android.content.Context
import android.os.Bundle
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import org.mariotaku.twidere.loader.CursorSupportUsersLoader
import org.mariotaku.twidere.loader.MutesUsersLoader
import org.mariotaku.twidere.model.UserKey

/**
 * Created by mariotaku on 2016/12/26.
 */
class FiltersImportMutesFragment : BaseFiltersImportFragment() {
    override fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean):
            CursorSupportUsersLoader {
        val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val loader = MutesUsersLoader(context, accountKey, adapter.data, fromUser)
        loader.cursor = nextCursor
        loader.page = nextPage
        return loader
    }
}