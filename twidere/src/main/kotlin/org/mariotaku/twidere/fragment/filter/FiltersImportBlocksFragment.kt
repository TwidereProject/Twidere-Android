package org.mariotaku.twidere.fragment.filter

import android.os.Bundle
import org.mariotaku.twidere.R
import org.mariotaku.twidere.data.fetcher.UsersFetcher
import org.mariotaku.twidere.data.fetcher.users.UserBlocksFetcher
import org.mariotaku.twidere.extension.linkHandlerTitle

class FiltersImportBlocksFragment : BaseFiltersImportFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        linkHandlerTitle = getString(R.string.title_select_users)
    }

    override fun onCreateUsersFetcher(): UsersFetcher {
        return UserBlocksFetcher()
    }
}
