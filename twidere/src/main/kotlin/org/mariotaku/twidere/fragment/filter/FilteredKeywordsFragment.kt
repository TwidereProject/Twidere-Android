package org.mariotaku.twidere.fragment.filter

import android.net.Uri
import org.mariotaku.twidere.fragment.BaseFiltersFragment
import org.mariotaku.twidere.provider.TwidereDataStore

class FilteredKeywordsFragment : BaseFiltersFragment() {

    override val contentUri: Uri
        get() = TwidereDataStore.Filters.Keywords.CONTENT_URI

    override val contentColumns: Array<String>
        get() = TwidereDataStore.Filters.Keywords.COLUMNS

}