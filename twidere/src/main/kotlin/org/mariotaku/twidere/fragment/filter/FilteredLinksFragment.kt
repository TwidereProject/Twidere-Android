package org.mariotaku.twidere.fragment.filter

import android.net.Uri
import org.mariotaku.twidere.fragment.BaseFiltersFragment
import org.mariotaku.twidere.provider.TwidereDataStore

class FilteredLinksFragment : BaseFiltersFragment() {

    override val contentColumns: Array<String>
        get() = TwidereDataStore.Filters.Links.COLUMNS

    override val contentUri: Uri
        get() = TwidereDataStore.Filters.Links.CONTENT_URI

}